package io.fairyproject.container;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.ContainerNodeScanner;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.util.Stacktrace;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ContainerPluginListener implements PluginListenerAdapter {

    private final ContainerContext containerContext;

    @Override
    public void onPluginEnable(Plugin plugin) {
        final Class<? extends Plugin> aClass = plugin.getClass();
        ContainerNode node = ContainerNode.create(plugin.getName());
        ContainerObj pluginObj = ContainerObj.of(aClass, plugin);

        plugin.setNode(node);
        node.addObj(pluginObj);
        Debug.log("Plugin " + plugin.getName() + " has been registered as ContainerObject.");

        try {
            List<String> classPaths = new ArrayList<>(this.containerContext.findClassPaths(aClass));
            classPaths.add(plugin.getDescription().getShadedPackage());

            ContainerNodeScanner scanner = this.containerContext.scanClasses();
            scanner.name(plugin.getName());
            scanner.classLoader(plugin.getPluginClassLoader());
            scanner.classPath(classPaths);
            scanner.excludePackage(Fairy.getFairyPackage());
            scanner.node(node);

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

            scanner.scan();
            this.containerContext.node().addChild(node);
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
