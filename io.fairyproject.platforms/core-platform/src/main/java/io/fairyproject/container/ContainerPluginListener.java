package io.fairyproject.container;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.SimpleContainerObject;
import io.fairyproject.container.scanner.ClassPathScanner;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ContainerPluginListener implements PluginListenerAdapter {

    private final ContainerContext containerContext;

    @Override
    public void onPluginEnable(Plugin plugin) {
        final Class<? extends Plugin> aClass = plugin.getClass();
        ContainerObject containerObject = new SimpleContainerObject(plugin, aClass);

        try {
            containerObject.bindWith(plugin);
            this.containerContext.registerObject(containerObject, false);
            ContainerContext.log("Plugin " + plugin.getName() + " has been registered as ContainerObject.");
        } catch (Throwable throwable) {
            ContainerContext.LOGGER.error("An error occurs while registering plugin", throwable);
            plugin.closeAndReportException();
            return;
        }

        try {
            final List<String> classPaths = this.containerContext.findClassPaths(aClass);
            classPaths.add(plugin.getDescription().getShadedPackage());
            final ClassPathScanner scanner = this.containerContext.scanClasses()
                    .name(plugin.getName())
                    .classLoader(plugin.getPluginClassLoader());

            if (Debug.UNIT_TEST) {
                // Hard coded, anyway to make it safer?
                final Path pathMain = Paths.get("build/classes/java/main").toAbsolutePath();
                if (Files.exists(pathMain))
                    scanner.url(pathMain.toUri().toURL());

                final Path pathTest = Paths.get("build/classes/java/test").toAbsolutePath();
                if (Files.exists(pathTest))
                    scanner.url(pathTest.toUri().toURL());
            } else {
                scanner.url(plugin.getClass().getProtectionDomain().getCodeSource().getLocation());
            }

            scanner
                    .classPath(classPaths)
                    .excludePackage(Fairy.getFairyPackage())
                    .included(containerObject)
                    .scanBlocking();

            if (scanner.getException() != null) {
                SneakyThrowUtil.sneakyThrow(scanner.getException());
            }
        } catch (Throwable throwable) {
            ContainerContext.LOGGER.error("Plugin " + plugin.getName() + " occurs error when doing class path scanning.", Stacktrace.simplifyStacktrace(throwable));
            plugin.closeAndReportException();
        }
    }

    @Override
    public void onPluginDisable(Plugin plugin) {
        Collection<ContainerObject> containerObjectList = this.containerContext.findDetailsBindWith(plugin);
        try {
            this.containerContext.lifeCycle(LifeCycle.PRE_DESTROY, containerObjectList);
        } catch (Throwable throwable) {
            ContainerContext.LOGGER.error(throwable);
        }

        containerObjectList.forEach(containerObject -> {
            ContainerContext.log("ContainerObject " + containerObject.getType() + " Disabled, due to " + plugin.getName() + " being disabled.");

            containerObject.closeAndReportException();
        });

        try {
            this.containerContext.lifeCycle(LifeCycle.POST_DESTROY, containerObjectList);
        } catch (Throwable throwable) {
            ContainerContext.LOGGER.error(throwable);
        }
    }

    @Override
    public int priority() {
        return ContainerContext.PLUGIN_LISTENER_PRIORITY;
    }
}
