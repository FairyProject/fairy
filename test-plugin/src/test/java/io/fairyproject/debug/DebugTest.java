package io.fairyproject.debug;

import be.seeseemelk.mockbukkit.Coordinate;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.fairyproject.tests.bukkit.BukkitAssert;
import io.fairyproject.tests.bukkit.BukkitTestingBase;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class DebugTest extends BukkitTestingBase {

    @Test
    public void testBlockBreak() {
        final PlayerMock player = SERVER.addPlayer();
        fireEvent(new PlayerJoinEvent(player, ""));

        final WorldMock world = SERVER.addSimpleWorld("world");

        final BlockBreakEvent event = player.simulateBlockBreak(world.createBlock(new Coordinate()));
        Assert.assertNotNull(event);
        BukkitAssert.assertCancelled(event);
    }

    private void fireEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }

            try {
                registration.callEvent(event);
            } catch (AuthorNagException ex) {
                Plugin plugin = registration.getPlugin();

                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

}
