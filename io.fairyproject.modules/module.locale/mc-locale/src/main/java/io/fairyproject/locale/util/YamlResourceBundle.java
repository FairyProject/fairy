package io.fairyproject.locale.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public YamlResourceBundle(Reader reader) {
        this.entries = StreamSupport.stream(new Yaml().loadAll(reader).spliterator(), false)
                .flatMap(this::parseDoc)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public static class Control extends ResourceBundle.Control {

        public static final Control INSTANCE = new Control();

        @Override
        public List<String> getFormats(String baseName) {
            return ImmutableList.of("yaml", "yml");
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            Preconditions.checkArgument(getFormats(baseName).contains(format));
            final String bundleName = toBundleName(baseName, locale);
            final String resourceName = toResourceName(bundleName, format);
            final URL resource = loader.getResource(resourceName);
            if (resource == null) return null;
            final URLConnection connection = resource.openConnection();
            if (reload) connection.setUseCaches(false);
            return new YamlResourceBundle(connection.getInputStream());
        }
    }

    private Stream<Pair<String, Object>> parseDoc(Object obj) {
        Preconditions.checkArgument(obj instanceof Map);
        return ((Map<?, ?>) obj).entrySet().stream()
                .flatMap(entry -> parseNode(entry.getKey().toString(), entry.getValue(), Collections.emptyList()));
    }

    private Stream<Pair<String, Object>> parseNode(String key, Object value, List<Object> ancestors) {
        if (value instanceof Map) {
            return parseMapNode(key, (Map<String, Object>) value, ancestors);
        }
        if (value instanceof List) {
            value = ((List<?>) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
        }
        return Stream.of(Pair.of(key, value));
    }

    private Stream<Pair<String, Object>> parseMapNode(String key, Map<String, Object> value, List<Object> ancestors) {
        if (ancestors.stream().anyMatch(it -> it == value)) return Stream.empty();
        final ArrayList<Object> newList = new ArrayList<>(ancestors);
        newList.add(value);
        return value.entrySet().stream()
                .flatMap(entry -> parseNode(key + "." + entry.getKey(), entry.getValue(), newList));
    }

    private Stream<Pair<String, Object>> parseListNode(String key, List<Object> value, List<Object> ancestors) {
        if (ancestors.stream().anyMatch(it -> it == value)) return Stream.empty();
        final ArrayList<Object> newList = new ArrayList<>(ancestors);
        newList.add(value);

        Stream<Pair<String, Object>> strings;
        if (value.isEmpty()) {
            strings = Stream.empty();
        } else {
            label: {
                List<String> list = new ArrayList<>();
                for (Object o : value) {
                    if (!(o instanceof String)) {
                        strings = Stream.empty();
                        break label;
                    }
                    list.add((String) o);
                }
                strings = Stream.of(Pair.of(key, list.toArray(new Object[0])));
            }
        }

        AtomicInteger indexes = new AtomicInteger(0);
        final Stream<Pair<String, Object>> stream = Stream.of(value.toArray(new Object[0]))
                .flatMap(item -> {
                    return parseNode(key + "[" + indexes.getAndIncrement() + "]", item, newList);
                });
        return Stream.of(strings, stream).flatMap(s -> s);
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
