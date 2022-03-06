package io.fairyproject.bukkit.util;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.function.Function;

public final class PlayerLocaleUtil {
    private PlayerLocaleUtil() {}

    private static final Function<Player, String> GET_LOCALE_FUNCTION;

    static {
        Function<Player, String> function;
        try {
            // modern bukkit
            final Method modernMethod = Player.class.getDeclaredMethod("getLocale");
            function = player -> {
                try {
                    return (String) modernMethod.invoke(player);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (ReflectiveOperationException ex) {
            try {
                // legacy spigot method
                Method legacyMethod = Player.Spigot.class.getMethod("getLocale");
                function = player -> {
                    try {
                        return (String) legacyMethod.invoke(player.spigot());
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                };
            } catch (ReflectiveOperationException e) {
                // fallback
                function = player -> null;
            }
        }
        GET_LOCALE_FUNCTION = function;
    }

    public static String getLocale(Player player) {
        return GET_LOCALE_FUNCTION.apply(player);
    }

}
