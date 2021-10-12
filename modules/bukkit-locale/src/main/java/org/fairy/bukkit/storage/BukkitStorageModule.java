package org.fairy.bukkit.storage;

import org.fairy.module.Depend;
import org.fairy.module.Modular;

@Modular(
        value = "bukkit-storage",
        abstraction = false,
        depends = @Depend("core-storage")
)
public class BukkitStorageModule {
}
