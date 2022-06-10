package io.fairyproject.container;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.object.SimpleContainerObj;
import io.fairyproject.container.scanner.ClassPathScanner;
import io.fairyproject.log.Log;
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
        ContainerNode node = ContainerNode.create(plugin.getName());
        ContainerObj pluginObj = new SimpleContainerObj(plugin, aClass);

        plugin.setNode(node);

        try {
            pluginObj.bindWith(plugin);
            node.addObj(pluginObj);
            Debug.log("Plugin " + plugin.getName() + " has been registered as ContainerObject.");
        } catch (Throwable throwable) {
            Log.error("An error occurs while registering plugin", throwable);
            plugin.closeAndReportException();
            return;
        }

        try {
            List<String> classPaths = this.containerContext.findClassPaths(aClass);
            classPaths.add(plugin.getDescription().getShadedPackage());

            ClassPathScanner scanner = this.containerContext.scanClasses()
                    .name(plugin.getName())
                    .classLoader(plugin.getPluginClassLoader())
                    .classPath(classPaths)
                    .excludePackage(Fairy.getFairyPackage())
                    .included(pluginObj)
                    .node(node);

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

            scanner.scanBlocking();

            if (scanner.getException() != null) {
                SneakyThrowUtil.sneakyThrow(scanner.getException());
            }
        } catch (Throwable throwable) {
            Log.error("Plugin " + plugin.getName() + " occurs error when doing class path scanning.", Stacktrace.simplifyStacktrace(throwable));
            plugin.closeAndReportException();
        }
    }

    @Override
    public void onPluginDisable(Plugin plugin) {
        final ContainerNode node = plugin.getNode();
        if (node != null) {
            node.closeAndReportException();
        }
    }

    @Override
    public int priority() {
        return ContainerContext.PLUGIN_LISTENER_PRIORITY;
    }
}
