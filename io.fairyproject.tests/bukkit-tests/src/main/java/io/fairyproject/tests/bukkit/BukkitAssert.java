package io.fairyproject.tests.bukkit;

import lombok.experimental.UtilityClass;
import org.bukkit.event.Cancellable;
import org.junit.jupiter.api.Assertions;

@UtilityClass
public class BukkitAssert {

    public void assertCancelled(Cancellable cancellable) {
        Assertions.assertTrue(cancellable.isCancelled(), "Cancellable is not cancelled");
    }

    public void assertNotCancelled(Cancellable cancellable) {
        Assertions.assertTrue(cancellable.isCancelled(), "Cancellable is cancelled");
    }

}
