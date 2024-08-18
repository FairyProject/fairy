package io.fairyproject.bukkit.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyAdventureUtil {
    private static final Map<ChatColor, String> INDEX = new HashMap<>();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static Component deserialize(String text) {
        return MiniMessage.miniMessage().deserialize(fromLegacy(text, '&'));
    }

    public static String fromLegacy(String text, char code) {
        StringBuilder stringBuilder = new StringBuilder();
        int lastIndex = 0;

        Matcher matcher = HEX_PATTERN.matcher(text);
        while (matcher.find()) {
            stringBuilder.append(text, lastIndex, matcher.start());
            stringBuilder.append("<#").append(matcher.group(1)).append(">");
            lastIndex = matcher.end();
        }

        if (lastIndex < text.length()) {
            stringBuilder.append(text.substring(lastIndex));
        }

        text = stringBuilder.toString();
        stringBuilder.setLength(0);
        lastIndex = 0;

        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; ++i) {
            if ((b[i] == 167 || b[i] == code) && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {

                if (i > 0) {
                    stringBuilder.append(text, lastIndex, i);
                }

                ChatColor chatColor = ChatColor.getByChar(b[i + 1]);
                String s = INDEX.get(chatColor);
                stringBuilder.append("<").append(s).append(">");
                lastIndex = i + 2;
            }
        }

        if (lastIndex < text.length()) {
            stringBuilder.append(text.substring(lastIndex));
        }

        return stringBuilder.toString();
    }

    private LegacyAdventureUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

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
}
