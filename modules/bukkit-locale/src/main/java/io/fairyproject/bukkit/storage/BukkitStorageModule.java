package io.fairyproject.bukkit.storage;

import io.fairyproject.module.Depend;
import io.fairyproject.module.Modular;

@Modular(
        value = "bukkit-storage",
        abstraction = false,
        depends = @Depend("core-storage")
)
public class BukkitStorageModule {



}
