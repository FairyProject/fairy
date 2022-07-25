package io.fairytest;

import io.fairyproject.InMemoryRepository;
import io.fairyproject.Repository;
import io.fairyproject.StorageService;
import io.fairyproject.container.Autowired;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.persistence.Id;
import java.io.Serializable;

public class StorageServiceTest extends JUnitJupiterBase {

    @Autowired
    private StorageService storageService;

    @SuppressWarnings("unused")
    public static class TestPojo {

        @Id
        private String id;

    }

    @Test
    public void createRepositoryShouldBeInMemoryInJUnit() {
        final Repository<TestPojo, Serializable> repository = this.storageService.createRepository("test", TestPojo.class);

        Assertions.assertEquals(InMemoryRepository.class, repository.getClass());
    }

}
