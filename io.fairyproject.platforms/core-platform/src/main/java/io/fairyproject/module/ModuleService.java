package io.fairyproject.module;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.bean.*;
import io.fairyproject.util.FairyVersion;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.entry.Entry;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
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
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Service(name = "module")
public class ModuleService {

    @Autowired
    private static BeanContext BEAN_CONTEXT;

    public static final int PLUGIN_LISTENER_PRIORITY = BeanContext.PLUGIN_LISTENER_PRIORITY + 100;

    private static final Logger LOGGER = LogManager.getLogger(ModuleService.class);
    private static Map<String, Entry<Integer, FairyVersion>> PENDING_MODULES = new HashMap<>();

    public static void init() {
        PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
            @Override
            public void onPluginInitial(Plugin plugin) {
                if (PENDING_MODULES != null) {
                    plugin.getDescription().getModules().forEach((name, versionString) -> {
                        final FairyVersion version = FairyVersion.parse(versionString);
                        PENDING_MODULES.compute(name, (m, entry) -> {
                            if (entry == null) {
                                return new Entry<>(1, version);
                            }
                            return new Entry<>(entry.getKey() + 1, version.isAbove(entry.getValue()) ? version : entry.getValue());
                        });
                    });
                } else {
                    final ModuleService moduleService = Beans.get(ModuleService.class);
                    plugin.getDescription().getModules().forEach((name, version) -> {
                        final Module module = moduleService.registerByName(name, FairyVersion.parse(version));

                        if (module == null) {
                            plugin.closeAndReportException();
                            return;
                        }

                        module.addRef(); // add reference
                    });
                }
            }

            @Override
            public void onPluginDisable(Plugin plugin) {
                plugin.getDescription().getModules().forEach((name, versionString) -> {
                    if (PENDING_MODULES != null) {
                        final int refCount = PENDING_MODULES.compute(name, (m, entry) -> {
                            if (entry == null) {
                                return new Entry<>(0, FairyVersion.parse(versionString));
                            }
                            return new Entry<>(entry.getKey() - 1, entry.getValue());
                        }).getKey();

                        if (refCount <= 0) {
                            PENDING_MODULES.remove(name);
                        }
                    } else {
                        final ModuleService moduleService = Beans.get(ModuleService.class);
                        final Module module = moduleService.getByName(name);

                        if (module == null) {
                            return;
                        }

                        if (module.removeRef() <= 0) {
                            moduleService.unregister(module);
                        }
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

    @PostInitialize
    public void onPreInitialize() {
        for (Map.Entry<String, Entry<Integer, FairyVersion>> entry : PENDING_MODULES.entrySet()) {
            // Don't register if nothing referenced
            if (entry.getValue().getKey() <= 0) {
                continue;
            }
            this.registerByName(entry.getKey(), entry.getValue().getValue());
        }
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(ModuleController.class)
                .onEnable(obj -> {
                    ModuleController controller = (ModuleController) obj;
                    this.controllers.add(controller);

                    this.moduleByName.forEach((k, v) -> controller.onModuleLoad(v));
                })
                .onDisable(this.controllers::remove)
                .build());
        PENDING_MODULES = null;
    }

    @Nullable
    public Module getByName(String name) {
        return moduleByName.get(name);
    }

    public Module registerByName(String name, FairyVersion version) {
        return this.registerByName(name, version, false);
    }

    @Nullable
    public Module registerByName(String name, FairyVersion version, boolean canAbstract) {
        try {
            LOGGER.info("Registering module " + name + "...");
            final Path path = ModuleDownloader.download(new File(FairyPlatform.INSTANCE.getDataFolder(), "modules/" + name + ".jar").toPath(), name);

            return this.registerByPath(path, canAbstract);
        } catch (IOException e) {
            LOGGER.error("Unexpected IO error", e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public Module registerByPath(Path path) {
        return this.registerByPath(path, false);
    }

    @Nullable
    public Module registerByPath(Path path, boolean canAbstract) {
        Fairy.getPlatform().getClassloader().addJarToClasspath(path);

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
            final String classPath = jsonObject.get("classPath").getAsString();

            ClassLoader classLoader = new ModuleClassloader(path);

            module = new Module(name, classPath, classLoader);
            module.setAbstraction(jsonObject.get("abstraction").getAsBoolean());

            if (!canAbstract && module.isAbstraction()) {
                LOGGER.error("The module " + name + " is a abstraction module! Couldn't directly depend on the module.");
                return null;
            }

            final JsonArray depends = jsonObject.getAsJsonArray("depends");
            for (JsonElement element : depends) {
                JsonObject dependJson = element.getAsJsonObject();
                final String depend = dependJson.get("module").getAsString();
                final String[] split = depend.split(":");
                String moduleName = split[0];
                FairyVersion moduleVersion = FairyVersion.parse(split[1]);
                Module dependModule = this.getByName(moduleName);
                if (dependModule == null) {
                    dependModule = this.registerByName(moduleName, moduleVersion, true);
                    if (dependModule == null) {
                        LOGGER.error("Unable to find dependency module " + moduleName + " for " + name);
                        return null;
                    }
                    module.getDependModules().add(dependModule);
                }
                dependModule.addRef(); // add reference
            }

            this.moduleByName.put(name, module);
            this.onModuleLoad(module);
            BEAN_CONTEXT.scanClasses("Module " + name, classLoader, Collections.singletonList(this.getClass().getClassLoader()), Collections.singleton(classPath));
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
