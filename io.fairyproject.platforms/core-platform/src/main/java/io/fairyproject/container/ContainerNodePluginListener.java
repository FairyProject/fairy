package io.fairyproject.container;

import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.loader.PluginNodeLoader;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContainerNodePluginListener implements PluginListenerAdapter {

    private final ContainerContext context;

    @Override
    public void onPluginEnable(Plugin plugin) {
        ContainerNode node = new PluginNodeLoader(this.context, plugin).load();

        plugin.setNode(node);
        this.context.node().addChild(node);
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
