package io.fairyproject.bukkit.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LegacyAdventureUtilTest {

    @Test
    public void shouldParseLegacyChatColor() {
        assertEquals(
                LegacyAdventureUtil.deserialize("&6Hello"),
                Component.text("Hello").color(NamedTextColor.GOLD)
        );
    }

    @Test
    public void shouldParseHexColor() {
        assertEquals(
                LegacyAdventureUtil.deserialize("&#ff0000Hello"),
                Component.text("Hello").color(TextColor.color(255, 0, 0))
        );
    }

    @Test
    public void complexTestcases() {
        assertEquals(
                LegacyAdventureUtil.deserialize("&7[&r&#b92b27&lW&#aa235a&lO&#9a1b8d&lR&#8b13c0&lK&7]"),
                MiniMessage.miniMessage().deserialize("<gray>[<reset><#b92b27><bold>W<#aa235a><bold>O<#9a1b8d><bold>R<#8b13c0><bold>K<gray>]")
        );
    }
}

