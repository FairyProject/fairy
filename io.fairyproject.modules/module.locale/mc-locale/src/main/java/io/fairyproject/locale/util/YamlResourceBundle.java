package io.fairyproject.locale.util;

import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.entry.Entry;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Ported from kotlin to java https://github.com/akkinoc/yaml-resource-bundle
 *
 * @author akkinoc
 */
@RequiredArgsConstructor
public class YamlResourceBundle extends ResourceBundle {

    private final Map<String, Object> entries;

    public YamlResourceBundle(InputStream inputStream) {
        this.entries = StreamSupport.stream(new Yaml().loadAll(inputStream).spliterator(), false)
                .flatMap(this::parseDoc)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public YamlResourceBundle(Reader reader) {
        this.entries = StreamSupport.stream(new Yaml().loadAll(reader).spliterator(), false)
                .flatMap(this::parseDoc)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static class Control extends ResourceBundle.Control {

        public static final Control INSTANCE = new Control();

        @Override
        public List<String> getFormats(String baseName) {
            return Arrays.asList("yaml", "yml");
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            ConditionUtils.is(getFormats(baseName).contains(format), "Unsupported format: " + format);
            final String bundleName = toBundleName(baseName, locale);
            final String resourceName = toResourceName(bundleName, format);
            final URL resource = loader.getResource(resourceName);
            if (resource == null) return null;
            final URLConnection connection = resource.openConnection();
            if (reload) connection.setUseCaches(false);
            return new YamlResourceBundle(connection.getInputStream());
        }
    }

    private Stream<Entry<String, Object>> parseDoc(Object obj) {
        ConditionUtils.is(obj instanceof Map, "obj instanceof Map");
        return ((Map<?, ?>) obj).entrySet().stream()
                .flatMap(entry -> parseNode(entry.getKey().toString(), entry.getValue(), Collections.emptyList()));
    }

    private Stream<Entry<String, Object>> parseNode(String key, Object value, List<Object> ancestors) {
        if (value instanceof Map) {
            return parseMapNode(key, (Map<String, Object>) value, ancestors);
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return Stream.of(new Entry<>(key, list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"))));
        }
        return Stream.of(new Entry<>(key, value));
    }

    private Stream<Entry<String, Object>> parseMapNode(String key, Map<String, Object> value, List<Object> ancestors) {
        if (ancestors.stream().anyMatch(it -> it == value)) return Stream.empty();
        final ArrayList<Object> newList = new ArrayList<>(ancestors);
        newList.add(value);
        return value.entrySet().stream()
                .flatMap(entry -> parseNode(key + "." + entry.getKey(), entry.getValue(), newList));
    }

    @Override
    protected Object handleGetObject(@NotNull String key) {
        return this.entries.get(key).toString();
    }

    @NotNull
    @Override
    protected Set<String> handleKeySet() {
        return entries.keySet();
    }

    @NotNull
    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(this.entries.keySet());
    }
}
