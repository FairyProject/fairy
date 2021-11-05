package io.fairyproject.bukkit.locale;

import io.fairyproject.module.Depend;
import io.fairyproject.module.Modular;

@Modular(
        value = "bukkit-locale",
        depends = { @Depend("bukkit-storage"), @Depend("mc-locale") }
)
public class BukkitLocaleModule {
}
