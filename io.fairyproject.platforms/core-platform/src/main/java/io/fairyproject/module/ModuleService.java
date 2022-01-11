package io.fairyproject.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.FairyPlatform;
import io.fairyproject.container.*;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryRepository;
import io.fairyproject.module.relocator.JarRelocator;
import io.fairyproject.module.relocator.Relocation;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.FairyVersion;
import io.fairyproject.util.PreProcessBatch;
import io.fairyproject.util.Stacktrace;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

@Service
public class ModuleService {

    @Autowired
    private static ContainerContext CONTAINER_CONTEXT;

    public static final int PLUGIN_LISTENER_PRIORITY = ContainerContext.PLUGIN_LISTENER_PRIORITY + 100;

    private static final Logger LOGGER = LogManager.getLogger(ModuleService.class);
    private static final PreProcessBatch PENDING = PreProcessBatch.create();

    public static void init() {
        PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
            @Override
            public void onPluginInitial(Plugin plugin) {
                PENDING.runOrQueue(plugin.getName(), () -> {
                    final ModuleService moduleService = Containers.get(ModuleService.class);

                    // We will get all the paths first for relocation.
                    Map<String, Path> paths = new LinkedHashMap<>();
                    plugin.getDescription().getModules().forEach(pair -> {
                        String name = pair.getKey();
                        String version = pair.getValue();

                        downloadModules(name, version, paths, false);
                    });

                    if (Debug.UNIT_TEST) {
                        // only useful for unit testing
                        try {
                            final Class<?> moduleClass = Class.forName("MODULE", true, plugin.getClassLoader());
                            final Field field = moduleClass.getDeclaredField("ALL");

                            final List<String> all = (List<String>) field.get(null);
                            for (String tag : all) {
                                String[] split = tag.split(":");
//                                String groupId = split[0]; // TODO
                                String artifactId = split[1];
                                String version = split[2];

                                downloadModules(artifactId, version, paths, false);
                            }
                        } catch (ClassNotFoundException ignored) {
                            ignored.printStackTrace();
                        } catch (Exception ex) {
                            throw new IllegalStateException("Failed to load modules for " + plugin.getName(), ex);
                        }
                    }

                    List<String> modulesOrdered = new ArrayList<>(paths.keySet());
                    Collections.reverse(modulesOrdered);

                    // Relocation entries from all included modules
                    final Path[] relocationEntries = paths.values().toArray(new Path[0]);

                    // Then push all paths
                    modulesOrdered.forEach(name -> {
                        final Path path = paths.get(name);
                        final Module module = moduleService.registerByPath(path, plugin, relocationEntries);

                        if (module == null) {
                            plugin.closeAndReportException();
                            return;
                        }

                        module.addRef(); // add reference
                        plugin.getLoadedModules().add(module);
                    });
                });
            }

            @Override
            public void onPluginDisable(Plugin plugin) {
                if (PENDING.remove(plugin.getName())) {
                    return;
                }
                final ModuleService moduleService = Containers.get(ModuleService.class);
                plugin.getLoadedModules().forEach(module -> {
                    if (module.removeRef() <= 0) {
                        moduleService.unregister(module);
                    }
                });
            }

            @Override
            public int priority() {
                return PLUGIN_LISTENER_PRIORITY;
            }
        });
    }

    private static void downloadModules(String name, String version, Map<String, Path> paths, boolean canAbstract) {
        if (paths.containsKey(name)) {
            return;
        }
        Path path;
        try {
            path = ModuleDownloader.download("io.fairyproject", name, version, null);
        } catch (Throwable throwable) {
            LOGGER.error("An error occurs while download module " + name, throwable);
            return;
        }

        paths.put(name, path);

        // We will read dependencies first.
        try {
            final JsonObject jsonObject = readModuleData(path);

            assert jsonObject != null;
            boolean abstraction = jsonObject.get("abstraction").getAsBoolean();

            if (!canAbstract && abstraction) {
                throw new IllegalArgumentException("The module " + name + " is a abstraction module! Couldn't directly depend on the module.");
            }

            final JsonArray depends = jsonObject.getAsJsonArray("depends");
            for (JsonElement element : depends) {
                final String[] split = element.getAsString().split(":");
                String moduleName = split[0];
                String moduleVersion = split[1];

                downloadModules(moduleName, moduleVersion, paths, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Map<String, Module> moduleByName = new ConcurrentHashMap<>();
    private final List<ModuleController> controllers = new ArrayList<>();

    public Collection<Module> all() {
        return this.moduleByName.values();
    }

    @PreInitialize
    public void onPreInitialize() {
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(ModuleController.class)
                .onEnable(obj -> {
                    ModuleController controller = (ModuleController) obj;
                    this.controllers.add(controller);

                    this.moduleByName.forEach((k, v) -> controller.onModuleLoad(v));
                })
                .onDisable(this.controllers::remove)
                .build());
    }

    @PostInitialize
    public void onPostInitialize() {
        PENDING.flushQueue();
    }

    @Nullable
    public Module getByName(String name) {
        return moduleByName.get(name);
    }

    @Nullable
    public Module registerByPath(Path path, Plugin plugin, Path... relocationEntries) {
        Module module = null;
        try {
            JsonObject jsonObject = readModuleData(path);
            if (jsonObject == null) {
                return null;
            }
            String name = jsonObject.get("name").getAsString();
            String classPath = !Debug.UNIT_TEST ? plugin.getDescription().getShadedPackage() + ".fairy" : "io.fairyproject";

            final List<Module> dependedModules = this.loadDependModules(jsonObject, name);
            if (dependedModules == null) {
                return null;
            }

            // Relocation after depended on modules are loaded
            final String fullFileName = path.getFileName().toString();
            final String fileName = FilenameUtils.getBaseName(fullFileName);

            Files.createDirectories(plugin.getDataFolder());

            Path shadedPath = path;
            if (!Debug.UNIT_TEST) {
                shadedPath = plugin.getDataFolder().resolve(fileName + "-remapped.jar");
                if (!Files.exists(shadedPath)) {
                    this.remap(path, shadedPath, plugin, dependedModules, relocationEntries);
                }
            }

            Fairy.getPlatform().getClassloader().addJarToClasspath(shadedPath);

            module = new Module(name, classPath, plugin, path, shadedPath);
            module.setAbstraction(jsonObject.get("abstraction").getAsBoolean());

            this.loadLibrariesAndExclusives(module, plugin, jsonObject);

            this.moduleByName.put(name, module);
            final Collection<String> excludedPackages = module.getExcludedPackages(this);

            // Scan classes
            final List<ContainerObject> details = CONTAINER_CONTEXT.scanClasses()
                    .name(plugin.getName() + "-" + module.getName())
                    .prefix(plugin.getName() + "-")
                    .classLoader(this.getClass().getClassLoader())
                    .excludePackage(excludedPackages)
                    .url(shadedPath.toUri().toURL())
                    .classPath(module.getClassPath())
                    .scan();
            details.forEach(bean -> bean.bindWith(plugin));

            this.onModuleLoad(module);
            return module;
        } catch (Exception e) {
            e.printStackTrace();

            if (module != null) {
                this.unregister(module);
            }
        }

        return null;
    }

    private void loadLibrariesAndExclusives(Module module, Plugin plugin, JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.getAsJsonObject("exclusive").entrySet()) {
            String exclude = entry.getKey();
            exclude = exclude.replace("io.fairyproject", plugin.getDescription().getShadedPackage() + ".fairy");

            module.getExclusives().put(entry.getValue().getAsString(), exclude);
        }

        for (JsonElement element : jsonObject.getAsJsonArray("libraries")) {
            final JsonObject libObject = element.getAsJsonObject();
            Library library = Library.builder()
                    .gradle(libObject.get("dependency").getAsString())
                    .repository(libObject.has("repository") ? new LibraryRepository(libObject.get("repository").getAsString()) : null)
                    .build();

            Fairy.getLibraryHandler().downloadLibraries(true, library);
        }
    }

    private static JsonObject readModuleData(Path path) throws IOException {
        final JarFile jarFile = new JarFile(path.toFile());
        final ZipEntry zipEntry = jarFile.getEntry("module.json");
        if (zipEntry == null) {
            LOGGER.error("Unable to find module.json from " + path);
            return null;
        }

        return new Gson().fromJson(new InputStreamReader(jarFile.getInputStream(zipEntry)), JsonObject.class);
    }

    private List<Module> loadDependModules(JsonObject jsonObject, String name) {
        List<Module> dependedModules = new ArrayList<>();
        final JsonArray depends = jsonObject.getAsJsonArray("depends");
        for (JsonElement element : depends) {
            final String[] split = element.getAsString().split(":");
            String moduleName = split[0];
            Module dependModule = this.getByName(moduleName);
            if (dependModule == null) {
                LOGGER.error("Unable to find dependency module " + moduleName + " for " + name + " BUT CONTINUE XD");
                //return null;
                continue;
            }
            dependedModules.add(dependModule);
            dependModule.addRef(); // add reference
        }
        return dependedModules;
    }

    private void remap(Path fromPath, Path finalPath, Plugin plugin, List<Module> dependedModules, Path[] relocationEntries) throws IOException {
        final Relocation relocation = new Relocation("io.fairyproject", plugin.getDescription().getShadedPackage() + ".fairy");
        relocation.setOnlyRelocateShaded(true);

        final Set<File> entries = dependedModules.stream()
                .map(Module::getNotShadedPath)
                .map(Path::toFile)
                .collect(Collectors.toCollection(HashSet::new));
        // also, included provided entries
        entries.addAll(Stream.of(relocationEntries).map(Path::toFile).collect(Collectors.toList()));

        new JarRelocator(fromPath.toFile(), finalPath.toFile(), Collections.singletonList(relocation), entries).run();
    }

    public void unregister(@NonNull Module module) {
        this.moduleByName.remove(module.getName());
        this.onModuleUnload(module);
        module.closeAndReportException();

        for (Module dependModule : module.getDependModules()) {
            if (dependModule.removeRef() <= 0) {
                this.unregister(dependModule);
            }
        }
    }

    private void onModuleLoad(Module module) {
        for (ModuleController controller : this.controllers) {
            try {
                controller.onModuleLoad(module);
            } catch (Throwable throwable) {
                Stacktrace.print(throwable);
            }
        }
    }

    private void onModuleUnload(Module module) {
        for (ModuleController controller : this.controllers) {
            try {
                controller.onModuleUnload(module);
            } catch (Throwable throwable) {
                Stacktrace.print(throwable);
            }
        }
    }

}
