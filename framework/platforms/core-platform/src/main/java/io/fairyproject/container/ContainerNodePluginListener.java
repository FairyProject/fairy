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
            context.nodeDestroyer().destroy(node);
        }
    }

    @Override
    public int priority() {
        return ContainerContext.PLUGIN_LISTENER_PRIORITY;
    }
}
