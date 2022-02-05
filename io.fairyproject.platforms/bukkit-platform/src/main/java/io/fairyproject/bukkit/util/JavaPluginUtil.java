package io.fairyproject.bukkit.util;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class JavaPluginUtil {

    /**
     * For unit testing
     */
    private JavaPlugin CURRENT_PLUGIN;

    @Nullable
    public JavaPlugin getProvidingPlugin(@NotNull Class<?> clazz) {
        if (CURRENT_PLUGIN != null) {
            return CURRENT_PLUGIN;
        }

        try {
            return JavaPlugin.getProvidingPlugin(clazz);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public void setCurrentPlugin(@NotNull JavaPlugin plugin) {
        CURRENT_PLUGIN = plugin;
    }

}
