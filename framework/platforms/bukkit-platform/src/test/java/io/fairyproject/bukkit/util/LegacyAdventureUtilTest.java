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
                LegacyAdventureUtil.decode("&6Hello"),
                Component.text("Hello").color(NamedTextColor.GOLD)
        );
    }

    @Test
    public void shouldParseHexColor() {
        assertEquals(
                LegacyAdventureUtil.decode("&#ff0000Hello"),
                Component.text("Hello").color(TextColor.color(255, 0, 0))
        );
    }

    @Test
    public void nullTextIsEmpty() {
        assertEquals("", LegacyAdventureUtil.fromLegacy(null, '&'));
    }

    @Test
    public void invalidHexStaysLiteral() {
        assertEquals("&#ff00Hello", LegacyAdventureUtil.fromLegacy("&#ff00Hello", '&'));
    }

    @Test
    public void codeIsNotTreatedAsRegex() {
        assertEquals("<#ff0000>Hello", LegacyAdventureUtil.fromLegacy("*#ff0000Hello", '*'));
        assertEquals("x#ff0000Hello", LegacyAdventureUtil.fromLegacy("x#ff0000Hello", '.'));
    }

    @Test
    public void complexTestcases() {
        assertEquals(
                LegacyAdventureUtil.decode("&7[&r&#b92b27&lW&#aa235a&lO&#9a1b8d&lR&#8b13c0&lK&7]"),
                MiniMessage.miniMessage().deserialize("<gray>[<reset><#b92b27><bold>W<#aa235a><bold>O<#9a1b8d><bold>R<#8b13c0><bold>K<gray>]")
        );
    }
}