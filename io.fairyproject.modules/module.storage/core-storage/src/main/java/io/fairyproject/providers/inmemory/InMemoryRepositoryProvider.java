package io.fairyproject.providers.inmemory;

import io.fairyproject.AbstractRepositoryProvider;
import io.fairyproject.InMemoryRepository;
import io.fairyproject.Repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InMemoryRepositoryProvider extends AbstractRepositoryProvider {

    public static final InMemoryRepositoryProvider INSTANCE = new InMemoryRepositoryProvider("");

    public InMemoryRepositoryProvider(String id) {
        super(id);
    }

    @Override
    public void build0() {

    }

    @Override
    public <E, ID extends Serializable> Repository<E, ID> createRepository(Class<E> entityType, String repoId) {
        return new InMemoryRepository<>(this, entityType, repoId);
    }

    @Override
    public Map<String, String> getDefaultOptions() {
        return new HashMap<>();
    }

    @Override
    public void registerOptions(Map<String, String> map) {

    }

    @Override
    public void close() throws Exception {

    }
}
