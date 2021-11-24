package io.fairyproject.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.container.*;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryRepository;
import io.fairyproject.module.relocator.JarRelocator;
import io.fairyproject.module.relocator.Relocation;
import io.fairyproject.util.FairyVersion;
import io.fairyproject.util.PreProcessBatch;
import io.fairyproject.util.Stacktrace;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.fairyproject.Fairy;
import io.fairyproject.FairyPlatform;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

@Service(name = "module")
public class ModuleService {

    @Autowired
    private static ContainerContext BEAN_CONTEXT;

    public static final int PLUGIN_LISTENER_PRIORITY = ContainerContext.PLUGIN_LISTENER_PRIORITY + 100;

    private static final Logger LOGGER = LogManager.getLogger(ModuleService.class);
    private static PreProcessBatch PENDING = PreProcessBatch.create();

    public static void init() {
        PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
            @Override
            public void onPluginInitial(Plugin plugin) {
                PENDING.runOrQueue(plugin.getName(), () -> {
                    final ModuleService moduleService = Containers.get(ModuleService.class);
                    plugin.getDescription().getModules().forEach(pair -> {
                        final String name = pair.getKey();
                        final String version = pair.getValue();
                        final Module module = moduleService.registerByName(name, FairyVersion.parse(version), plugin);

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
                if (PENDING != null) {
                    PENDING.remove(plugin.getName());
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

    public Module registerByName(String name, FairyVersion version, Plugin plugin) {
        return this.registerByName(name, version, false, plugin);
    }

    @Nullable
    public Module registerByName(String name, FairyVersion version, boolean canAbstract, Plugin plugin) {
        try {
            LOGGER.info("Registering module " + name + "...");
            final Path path = ModuleDownloader.download(new File(FairyPlatform.INSTANCE.getDataFolder(), "modules/" + name + "-" + version.toString() + ".jar").toPath(), name, version.toString());

            return this.registerByPath(path, canAbstract, plugin);
        } catch (IOException e) {
            LOGGER.error("Unexpected IO error", e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public Module registerByPath(Path path, Plugin plugin) {
        return this.registerByPath(path, false, plugin);
    }

    @Nullable
    public Module registerByPath(Path path, boolean canAbstract, Plugin plugin) {
        Module module = null;
        try {
            final JarFile jarFile = new JarFile(path.toFile());
            final ZipEntry zipEntry = jarFile.getEntry("module.json");
            if (zipEntry == null) {
                LOGGER.error("Unable to find module.json from " + path);
                return null;
            }

            final JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(jarFile.getInputStream(zipEntry)), JsonObject.class);
            final String name = jsonObject.get("name").getAsString();
            final String classPath = plugin.getDescription().getShadedPackage() + ".fairy";

            boolean abstraction = jsonObject.get("abstraction").getAsBoolean();

            if (!canAbstract && abstraction) {
                LOGGER.error("The module " + name + " is a abstraction module! Couldn't directly depend on the module.");
                return null;
            }

            List<Module> dependedModules = new ArrayList<>();
            final JsonArray depends = jsonObject.getAsJsonArray("depends");
            for (JsonElement element : depends) {
                final String[] split = element.getAsString().split(":");
                String moduleName = split[0];
                FairyVersion moduleVersion = FairyVersion.parse(split[1]);
                Module dependModule = this.getByName(moduleName);
                if (dependModule == null) {
                    dependModule = this.registerByName(moduleName, moduleVersion, true, plugin);
                    if (dependModule == null) {
                        LOGGER.error("Unable to find dependency module " + moduleName + " for " + name);
                        return null;
                    }
                }
                dependedModules.add(dependModule);
                dependModule.addRef(); // add reference
            }

            // Relocation after depended on modules are loaded
            final String fullFileName = path.getFileName().toString();
            final String fileName = FilenameUtils.getBaseName(fullFileName);

            Files.createDirectories(plugin.getDataFolder());

            Path notShadedPath = path;
            final Path finalPath = plugin.getDataFolder().resolve(fileName + "-remapped.jar");
            if (!Files.exists(finalPath)) {
                final Relocation relocation = new Relocation("io.fairyproject", plugin.getDescription().getShadedPackage() + ".fairy");
                relocation.setOnlyRelocateShaded(true);
                new JarRelocator(path.toFile(), finalPath.toFile(), Collections.singletonList(relocation), dependedModules.stream()
                        .map(Module::getNotShadedPath)
                        .map(Path::toFile)
                        .collect(Collectors.toSet())).run();
            }
            path = finalPath;
            Fairy.getPlatform().getClassloader().addJarToClasspath(path);

            final ModuleClassloader classLoader = new ModuleClassloader(path);
            module = new Module(name, classPath, classLoader, plugin, notShadedPath, path);
            module.setAbstraction(jsonObject.get("abstraction").getAsBoolean());

            for (Map.Entry<String, JsonElement> entry : jsonObject.getAsJsonObject("exclusive").entrySet()) {
                module.getExclusives().put(entry.getValue().getAsString(), entry.getKey());
            }

            for (JsonElement element : jsonObject.getAsJsonArray("libraries")) {
                final JsonObject libObject = element.getAsJsonObject();
                Library library = Library.builder()
                        .gradle(libObject.get("dependency").getAsString())
                        .repository(libObject.has("repository") ? new LibraryRepository(libObject.get("repository").getAsString()) : null)
                        .build();

                Fairy.getLibraryHandler().downloadLibraries(true, library);
            }

            this.moduleByName.put(name, module);
            this.onModuleLoad(module);

            // Scan classes
            final List<ContainerObject> details = BEAN_CONTEXT.scanClasses()
                    .name(plugin.getName() + "-" + module.getName())
                    .prefix(plugin.getName() + "-")
                    .mainClassloader(classLoader)
                    .classLoader(this.getClass().getClassLoader())
                    .classPath(module.getClassPath())
                    .scan();
            details.forEach(bean -> bean.bindWith(plugin));
            return module;
        } catch (Exception e) {
            e.printStackTrace();

            if (module != null) {
                this.unregister(module);
            }
        }

        return null;
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
