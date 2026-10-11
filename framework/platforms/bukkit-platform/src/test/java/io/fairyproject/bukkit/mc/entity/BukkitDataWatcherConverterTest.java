package io.fairyproject.bukkit.mc.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BukkitDataWatcherConverterTest {
    @Test
    void sparsePaperStorageSkipsHolesAndPreservesDirtyEntries() throws Exception {
        DataItem first = new DataItem("first");
        DataItem last = new DataItem("last");
        ArrayWatcher watcher = new ArrayWatcher(new DataItem[]{null, first, null, last});
        Method getValue = DataItem.class.getMethod("value");

        List<Object> values = BukkitDataWatcherConverter.collectDataValues(watcher, DataItem.class, getValue);
        assertEquals(Arrays.asList("first", "last"), values);
        assertEquals(values, BukkitDataWatcherConverter.collectDataValues(watcher, DataItem.class, getValue));
        assertTrue(first.dirty);
        assertTrue(last.dirty);
        assertNull(watcher.items[0]);
        assertSame(first, watcher.items[1]);
    }

    @Test
    void spigotMapStorageIncludesCleanAndDirtyEntries() throws Exception {
        MapWatcher watcher = new MapWatcher();
        DataItem clean = new DataItem("clean");
        clean.dirty = false;
        DataItem dirty = new DataItem("dirty");
        watcher.items.put(0, clean);
        watcher.items.put(3, dirty);
        assertEquals(Arrays.asList("clean", "dirty"), BukkitDataWatcherConverter.collectDataValues(
                watcher, DataItem.class, DataItem.class.getMethod("value")));
        assertFalse(clean.dirty);
        assertTrue(dirty.dirty);
        assertSame(clean, watcher.items.get(0));
        assertSame(dirty, watcher.items.get(3));
        assertEquals(Arrays.asList(clean, dirty), BukkitDataWatcherConverter.collectDataValues(
                watcher, DataItem.class, null));
        assertTrue(dirty.dirty);
    }

    public static class DataItem {
        private final String value;
        private boolean dirty = true;

        DataItem(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    private static class ArrayWatcher {
        private final DataItem[] items;

        ArrayWatcher(DataItem[] items) {
            this.items = items;
        }
    }

    private static class MapWatcher {
        // NMS also has static maps; only this watcher's instance entries belong in the packet.
        private static final Map<Integer, DataItem> REGISTRY = new LinkedHashMap<>();

        static {
            REGISTRY.put(100, new DataItem("unrelated registry entry"));
        }

        private final Map<Integer, DataItem> items = new LinkedHashMap<>();
    }
}
