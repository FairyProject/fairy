package io.fairyproject.bukkit.util;

import io.fairyproject.bukkit.plugin.impl.RootJavaPluginIdentifier;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class JavaPluginUtil {

    @Nullable
    public JavaPlugin getProvidingPlugin(@NotNull Class<?> clazz) {
        return RootJavaPluginIdentifier.getInstance().findByClass(clazz);
    }

}
