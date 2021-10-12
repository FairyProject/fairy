package org.fairy.bukkit;

import org.fairy.module.Depend;
import org.fairy.module.Modular;

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
