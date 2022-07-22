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
        // Nothing to do
    }

    @Override
    public <E, I extends Serializable> Repository<E, I> createRepository(Class<E> entityType, String repoId) {
        return new InMemoryRepository<>(this, entityType, repoId);
    }

    @Override
    public Map<String, String> getDefaultOptions() {
        return new HashMap<>();
    }

    @Override
    public void registerOptions(Map<String, String> map) {
        // Nothing to do
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }
}
