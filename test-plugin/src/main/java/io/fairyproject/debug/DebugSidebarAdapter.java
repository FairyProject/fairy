package io.fairyproject.debug;

import io.fairyproject.container.Component;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.sidebar.SidebarAdapter;
import io.fairyproject.util.CC;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@Component
public class DebugSidebarAdapter implements SidebarAdapter {

    private final String title;
    private final ChatColor[] titleColors;

    private int offset;

    public DebugSidebarAdapter() {
        this.title = "SIDEBAR";
        this.titleColors = new ChatColor[this.title.length()];
        this.setTitleColorOffset(0);
    }

    @Override
    public net.kyori.adventure.text.Component getTitle(MCPlayer mcPlayer) {
        offset++;
        this.setTitleColorOffset(offset);

        net.kyori.adventure.text.Component component = net.kyori.adventure.text.Component.empty();
        final char[] charArray = this.title.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            component = component.append(net.kyori.adventure.text.Component.text(this.titleColors[i].toString() + charArray[i]));
        }

        System.out.println(MCAdventure.asLegacyString(component, mcPlayer.getLocale()).length());
        return component;
    }

    @Override
    public List<net.kyori.adventure.text.Component> getLines(MCPlayer mcPlayer) {
        // Port MCPlayer to bukkit player
        final Player player = mcPlayer.as(Player.class);
        final Location location = player.getLocation();

        return Arrays.asList(
                // ----------------------
                net.kyori.adventure.text.Component.text(CC.SB_BAR),

                // Welcome: PLAYER!
                net.kyori.adventure.text.Component.text("Welcome ", NamedTextColor.AQUA)
                        .append(net.kyori.adventure.text.Component.text(mcPlayer.getName() + "!", NamedTextColor.WHITE)),

                // Your version: V1_8
                net.kyori.adventure.text.Component.text("Your version: ", NamedTextColor.AQUA)
                        .append(net.kyori.adventure.text.Component.text(mcPlayer.getVersion().toString(), NamedTextColor.WHITE)),

                // Your gamemode: SURVIVAL
                net.kyori.adventure.text.Component.text("Your gamemode: ", NamedTextColor.AQUA)
                        .append(net.kyori.adventure.text.Component.text(player.getGameMode().toString(), NamedTextColor.WHITE)),

                // Your location: 0, 0, 0
                net.kyori.adventure.text.Component.text("Your location: ", NamedTextColor.AQUA)
                        .append(net.kyori.adventure.text.Component.text(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ(), NamedTextColor.WHITE)),

                // ----------------------
                net.kyori.adventure.text.Component.text(CC.SB_BAR)
        );
    }

    private void setTitleColorOffset(int offset) {
        final int length = this.title.length();
        List<ChatColor> colors = Arrays.asList(ChatColor.values());
        for (int i = 0; i < length; i++) {
            this.titleColors[i] = colors.get((offset + i) % length);
        }
    }

}
