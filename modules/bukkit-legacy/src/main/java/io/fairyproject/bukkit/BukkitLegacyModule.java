package io.fairyproject.bukkit;

import io.fairyproject.module.Depend;
import io.fairyproject.module.Modular;

@Modular(
        value = "bukkit-legacy",
        abstraction = false,
        depends = {
                @Depend("mc-abstract"),
                @Depend("bukkit-legacy"),
                @Depend("bukkit-timings")
        }
)
public class BukkitLegacyModule {
    /*
     * Empty class for marking @Modular
     *
     * TODO: remove and separate each package to it own modules or extensions
     */
}
