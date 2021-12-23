package io.fairyproject.tests.bukkit;

import lombok.experimental.UtilityClass;
import org.bukkit.event.Cancellable;
import org.junit.Assert;

@UtilityClass
public class BukkitAssert {

    public void assertCancelled(Cancellable cancellable) {
        Assert.assertTrue("Cancellable is not cancelled", cancellable.isCancelled());
    }

    public void assertNotCancelled(Cancellable cancellable) {
        Assert.assertTrue("Cancellable is cancelled", cancellable.isCancelled());
    }

}
