package io.fairyproject.bukkit.menu;

import io.fairyproject.module.Depend;
import io.fairyproject.module.Modular;

@Modular(
        value = "bukkit-menu",
        depends = @Depend("bukkit-items")
)
public class BukkitMenuModule {
}
