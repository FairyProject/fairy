package org.fairy;

import org.fairy.module.Depend;
import org.fairy.module.Modular;

@Modular(
        value = "core-legacy",
        abstraction = false,
        depends = @Depend("core-storage")
)
public class CoreLegacyModule {
}
