package io.fairyproject.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.*;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.scanner.ClassPathScanner;
import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryRepository;
import io.fairyproject.module.relocator.JarRelocator;
import io.fairyproject.module.relocator.Relocation;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.FairyVersion;
import io.fairyproject.util.PreProcessBatch;
import io.fairyproject.util.Stacktrace;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class ModuleService {

    public static ModuleService INSTANCE;

    @Autowired
    private static ContainerContext CONTAINER_CONTEXT;

    public static final int PLUGIN_LISTENER_PRIORITY = ContainerContext.PLUGIN_LISTENER_PRIORITY + 100;

    private static final Logger LOGGER = LogManager.getLogger(ModuleService.class);

    private final Map<String, Module> moduleByName = new ConcurrentHashMap<>();
    private final List<ModuleController> controllers = new ArrayList<>();

    final PreProcessBatch pendingProcessBatch = PreProcessBatch.create();

    public static void init() {
        ConditionUtils.check(INSTANCE == null, "Already initialized.");
        INSTANCE = new ModuleService();
        INSTANCE.load();
    }

    public Collection<Module> all() {
        return this.moduleByName.values();
    }

    private void load() {
        PluginManager.INSTANCE.registerListener(new ModulePluginListenerAdapter(this));

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

    public void enable() {
        this.pendingProcessBatch.flushQueue();
    }

    void downloadModules(String name, String version, ModuleDataList paths, boolean canAbstract) {
        Path path;
        try {
            path = ModuleDownloader.download("io.fairyproject", name, version, null);
        } catch (Throwable throwable) {
            LOGGER.error("An error occurs while download module " + name, throwable);
            return;
        }

        paths.add(new ModuleData(name, FairyVersion.parse(version), path));

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

    @PostInitialize
    public void onPostInitialize() {
        pendingProcessBatch.flushQueue();
    }

    @Nullable
    public Module getByName(String name) {
        return moduleByName.get(name);
    }

    @Nullable
    public Module load(ModuleData moduleData, PluginDescription pluginDescription, CompletableFuture<Plugin> pluginCompletableFuture) {
        Module module = null;
        try {
            JsonObject jsonObject = readModuleData(moduleData.getPath());
            if (jsonObject == null) {
                return null;
            }
            String name = jsonObject.get("name").getAsString();
            String classPath = !Debug.UNIT_TEST
                    ? pluginDescription.getShadedPackage() + ".fairy"
                    : "io.fairyproject";

            final List<Module> dependedModules = this.loadDependModules(jsonObject, name);
            if (dependedModules == null) {
                LOGGER.warn("Failed to load depended modules from " + name + " in " + pluginDescription.getName());
                return null;
            }

            Path shadedPath = moduleData.getShadedPath();

            module = new Module(name, classPath, pluginDescription, moduleData.getPath(), shadedPath);
            module.setAbstraction(jsonObject.get("abstraction").getAsBoolean());

            for (Module dependedModule : dependedModules) {
                dependedModule.addRef();
                module.getDependModules().add(dependedModule);
            }

            this.loadExclusives(module, pluginDescription, jsonObject);

            this.moduleByName.put(name, module);
            final Collection<String> excludedPackages = module.getExcludedPackages(this);

            // Scan classes
            final ClassPathScanner classPathScanner = CONTAINER_CONTEXT.scanClasses()
                    .name(pluginDescription.getName() + "-" + module.getName())
                    .prefix(pluginDescription.getName() + "-")
                    .classLoader(this.getClass().getClassLoader())
                    .excludePackage(excludedPackages)
                    .url(shadedPath.toUri().toURL())
                    .classPath(module.getClassPath());
            classPathScanner.scan();

            final List<ContainerObject> containerObjects = classPathScanner.getCompletedFuture().join();
            pluginCompletableFuture.whenComplete((plugin, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    containerObjects.forEach(bean -> bean.bindWith(plugin));
                }
            });

            this.onModuleLoad(module);
            return module;
        } catch (Exception e) {
            Stacktrace.simplifyStacktrace(e).printStackTrace();

            if (module != null) {
                this.unregister(module);
            }
        }

        return null;
    }

    void loadLibraries(JsonObject jsonObject) {
        List<Library> libraries = new ArrayList<>();

        for (JsonElement element : jsonObject.getAsJsonArray("libraries")) {
            final JsonObject libObject = element.getAsJsonObject();
            Library library = Library.builder()
                    .gradle(libObject.get("dependency").getAsString())
                    .repository(libObject.has("repository") ? new LibraryRepository(libObject.get("repository").getAsString()) : null)
                    .build();

            libraries.add(library);
        }

        Fairy.getLibraryHandler().downloadLibraries(true, libraries);
    }

    JsonObject readModuleData(Path path) throws IOException {
        final JarFile jarFile = new JarFile(path.toFile());
        final ZipEntry zipEntry = jarFile.getEntry("module.json");
        if (zipEntry == null) {
            LOGGER.error("Unable to find module.json from " + path);
            return null;
        }

        return new Gson().fromJson(new InputStreamReader(jarFile.getInputStream(zipEntry)), JsonObject.class);
    }

    List<ModuleData> loadDependModulesData(JsonObject jsonObject, String name, ModuleDataList moduleDataList) {
        List<ModuleData> dependedModules = new ArrayList<>();
        final JsonArray depends = jsonObject.getAsJsonArray("depends");
        for (JsonElement element : depends) {
            final String[] split = element.getAsString().split(":");
            String moduleName = split[0];
            ModuleData dependModule = moduleDataList.get(moduleName);
            if (dependModule == null) {
                throw new IllegalStateException("Unable to find dependency module " + moduleName + " for " + name);
            }
            dependedModules.add(dependModule);
        }
        return dependedModules;
    }

    void remap(Path fromPath, Path finalPath, PluginDescription description, List<ModuleData> dependedModules, Path[] relocationEntries) throws IOException {
        final Relocation relocation = new Relocation("io.fairyproject", description.getShadedPackage() + ".fairy");
        relocation.setOnlyRelocateShaded(true);

        final Set<File> entries = dependedModules.stream()
                .map(ModuleData::getPath)
                .map(Path::toFile)
                .collect(Collectors.toCollection(HashSet::new));
        // also, included provided entries
        entries.addAll(Stream.of(relocationEntries).map(Path::toFile).collect(Collectors.toList()));

        new JarRelocator(fromPath.toFile(), finalPath.toFile(), Collections.singletonList(relocation), entries).run();
    }

    private void loadExclusives(Module module, PluginDescription plugin, JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.getAsJsonObject("exclusive").entrySet()) {
            String exclude = entry.getKey();
            exclude = exclude.replace("io.fairyproject", plugin.getShadedPackage() + ".fairy");

            module.getExclusives().put(entry.getValue().getAsString(), exclude);
        }
    }

    private List<Module> loadDependModules(JsonObject jsonObject, String name) {
        List<Module> dependedModules = new ArrayList<>();
        final JsonArray depends = jsonObject.getAsJsonArray("depends");
        for (JsonElement element : depends) {
            final String[] split = element.getAsString().split(":");
            String moduleName = split[0];
            Module dependModule = this.getByName(moduleName);
            if (dependModule == null) {
                LOGGER.error("Unable to find dependency module " + moduleName + " for " + name);
                return null;
            }
            dependedModules.add(dependModule);
        }
        return dependedModules;
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

    @Getter
    @Setter
    static class ModuleData {

        private String name;
        private FairyVersion version;
        private Path path;
        private Path shadedPath;

        public ModuleData(String name, FairyVersion version, Path path) {
            this.name = name;
            this.version = version;
            this.path = path;
        }

        @Override
        public String toString() {
            return this.name + ":" + version;
        }
    }

    /**
     * This is the extended module data list to ensure module data order while changing the content whenever the newly added module has newer version
     */
    static class ModuleDataList extends ArrayList<ModuleData> {

        private final Map<String, ModuleData> map = new HashMap<>();

        @Override
        public boolean add(ModuleData moduleData) {
            final ModuleData previous = map.getOrDefault(moduleData.getName(), null);

            if (previous != null) {
                if (previous.getVersion().isBelow(moduleData.getVersion())) {
                    previous.setVersion(moduleData.getVersion());
                    previous.setPath(moduleData.getPath());
                }

                // swap order
                this.remove(previous);
                return super.add(previous);
            } else {
                map.put(moduleData.getName(), moduleData);
                return super.add(moduleData);
            }
        }

        public boolean containsKey(String name) {
            return this.map.containsKey(name);
        }

        public ModuleData get(String name) {
            return this.map.get(name);
        }

    }

}
