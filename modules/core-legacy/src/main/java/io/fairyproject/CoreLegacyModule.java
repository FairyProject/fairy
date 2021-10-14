package io.fairyproject;

import io.fairyproject.module.Depend;
import io.fairyproject.module.Modular;

@Modular(
        value = "core-legacy",
        abstraction = false,
        depends = @Depend("core-storage")
)
public class CoreLegacyModule {
}
