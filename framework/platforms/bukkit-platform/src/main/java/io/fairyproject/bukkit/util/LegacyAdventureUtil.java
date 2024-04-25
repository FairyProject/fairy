package io.fairyproject.bukkit.util;

import io.fairyproject.mc.MCAdventure;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@UtilityClass
public class LegacyAdventureUtil {

    private static final Map<ChatColor, String> INDEX = new HashMap<>();

    static {
        INDEX.put(ChatColor.BLACK, NamedTextColor.BLACK.toString());
        INDEX.put(ChatColor.DARK_BLUE, NamedTextColor.DARK_BLUE.toString());
        INDEX.put(ChatColor.DARK_GREEN, NamedTextColor.DARK_GREEN.toString());
        INDEX.put(ChatColor.DARK_AQUA, NamedTextColor.DARK_AQUA.toString());
        INDEX.put(ChatColor.DARK_RED, NamedTextColor.DARK_RED.toString());
        INDEX.put(ChatColor.DARK_PURPLE, NamedTextColor.DARK_PURPLE.toString());
        INDEX.put(ChatColor.GOLD, NamedTextColor.GOLD.toString());
        INDEX.put(ChatColor.GRAY, NamedTextColor.GRAY.toString());
        INDEX.put(ChatColor.DARK_GRAY, NamedTextColor.DARK_GRAY.toString());
        INDEX.put(ChatColor.BLUE, NamedTextColor.BLUE.toString());
        INDEX.put(ChatColor.GREEN, NamedTextColor.GREEN.toString());
        INDEX.put(ChatColor.AQUA, NamedTextColor.AQUA.toString());
        INDEX.put(ChatColor.RED, NamedTextColor.RED.toString());
        INDEX.put(ChatColor.LIGHT_PURPLE, NamedTextColor.LIGHT_PURPLE.toString());
        INDEX.put(ChatColor.YELLOW, NamedTextColor.YELLOW.toString());
        INDEX.put(ChatColor.WHITE, NamedTextColor.WHITE.toString());
        INDEX.put(ChatColor.MAGIC, TextDecoration.OBFUSCATED.toString());
        INDEX.put(ChatColor.BOLD, TextDecoration.BOLD.toString());
        INDEX.put(ChatColor.STRIKETHROUGH, TextDecoration.STRIKETHROUGH.toString());
        INDEX.put(ChatColor.UNDERLINE, TextDecoration.UNDERLINED.toString());
        INDEX.put(ChatColor.ITALIC, TextDecoration.ITALIC.toString());
        INDEX.put(ChatColor.RESET, "reset");
    }

    public String decodeAndLegacy(String legacyText) {
        return decodeAndLegacy(legacyText, TagResolver.empty());
    }

    public String decodeAndLegacy(String legacyText, TagResolver tagResolver) {
        if (legacyText == null || legacyText.isEmpty())
            return "";
        return MCAdventure.asLegacyString(
                decode(legacyText, tagResolver),
                Locale.ENGLISH
        );
    }

    public Component decode(String legacyText) {
        return decode(legacyText, TagResolver.empty());
    }

    public Component decode(String legacyText, TagResolver tagResolver) {
        if (legacyText == null)
            return Component.empty();
        return MiniMessage.miniMessage().deserialize(fromLegacy(legacyText, '&'), tagResolver);
    }

    public String fromLegacy(String text, char code) {
        if (text == null)
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        char[] b = text.toCharArray();

        int lastIndex = 0;
        for(int i = 0; i < b.length - 1; ++i) {
            if ((b[i] == '§' || b[i] == code) && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                if (i > 0)
                    stringBuilder.append(text, lastIndex, i);
                final ChatColor chatColor = ChatColor.getByChar(b[i + 1]);
                final String s = INDEX.get(chatColor);

                stringBuilder.append("<").append(s).append(">");
                lastIndex = i + 2;
            }
        }

        if (lastIndex < text.length()) {
            stringBuilder.append(text.substring(lastIndex));
        }
        return stringBuilder.toString();
    }

}
