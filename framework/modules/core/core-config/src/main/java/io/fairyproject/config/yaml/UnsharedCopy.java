package io.fairyproject.config.yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A copy of a converted configuration in which no map, list or set appears twice.
 * <p>
 * snakeyaml writes an object it meets a second time as an anchor and an alias, but by default
 * refuses to load more than 50 aliases, so a configuration whose fields share one instance (an
 * empty {@code Collections.emptyList()} in every entry of a map, say) is saved fine and then
 * fails to load with "Number of aliases for non-scalar nodes exceeds the specified max=50".
 */
final class UnsharedCopy {

    private UnsharedCopy() {
    }

    static Object of(Object value) {
        if (value instanceof Map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                copy.put(of(entry.getKey()), of(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof List) {
            List<Object> copy = new ArrayList<>(((List<?>) value).size());
            for (Object element : (List<?>) value) {
                copy.add(of(element));
            }
            return copy;
        }
        if (value instanceof Set) {
            Set<Object> copy = new LinkedHashSet<>();
            for (Object element : (Set<?>) value) {
                copy.add(of(element));
            }
            return copy;
        }
        return value;
    }
}
