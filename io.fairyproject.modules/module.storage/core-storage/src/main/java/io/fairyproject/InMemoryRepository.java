package io.fairyproject;

import io.fairyproject.pojo.PojoEx;
import io.fairyproject.pojo.PojoMapper;
import io.fairyproject.pojo.PojoProperty;
import io.fairyproject.providers.inmemory.InMemoryRepositoryProvider;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryRepository<T, ID extends Serializable> extends AbstractRepository<T, ID, InMemoryRepositoryProvider> {

    private Map<ID, T> map;
    private PojoMapper<T> pojoMapper;

    public InMemoryRepository(InMemoryRepositoryProvider repositoryProvider, Class<T> type, String repoId) {
        super(repositoryProvider, type, repoId);
    }

    @Override
    public void init() {
        this.map = new ConcurrentHashMap<>();

        this.pojoMapper = PojoMapper.createDatabase(this.type());
        try {
            this.pojoMapper.init();
        } catch (ReflectiveOperationException e) {
            SneakyThrowUtil.sneakyThrow(e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public boolean existsById(ID id) {
        return false;
    }

    @Override
    public Iterable<T> findAll() {
        return this.map.values();
    }

    @Override
    public Iterable<T> findAllById(List<ID> list) {
        return this.map.entrySet().stream()
                .filter(entry -> list.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return this.map.size();
    }

    @Override
    public void deleteById(ID id) {
        this.map.remove(id);
    }

    @Override
    public void deleteAll() {
        this.map.clear();
    }

    private Stream<Map.Entry<ID, T>> byQuery(String query, Object value) {
        final PojoProperty property = this.pojoMapper.getProperty(query);
        ConditionUtils.notNull(property, "Couldn't find property with name " + query);

        return this.map.entrySet().stream()
                .filter(entry -> Objects.equals(property.get(entry.getValue()), value));
    }

    @Override
    public void deleteByQuery(String query, Object value) {
        this.byQuery(query, value)
                .findFirst()
                .ifPresent(entry -> this.map.remove(entry.getKey()));
    }

    @Override
    public Optional<T> findByQuery(String query, Object value) {
        return this.byQuery(query, value)
                .map(Map.Entry::getValue)
                .findFirst();
    }

    @Override
    public <S extends T> S save(S pojo) {
        final PojoProperty property = this.pojoMapper.getOrThrow(PojoEx.PRIMARY_KEY);
        final ID id = (ID) property.get(pojo);

        this.map.put(id, pojo);
        return pojo;
    }
}
