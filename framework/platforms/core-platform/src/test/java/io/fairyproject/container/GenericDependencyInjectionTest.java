package io.fairyproject.container;

import io.fairyproject.container.object.singleton.SingletonObjectRegistryImpl;
import io.fairyproject.container.type.TypeDescriptor;
import io.fairyproject.container.util.GenericTypeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for generic type resolution in dependency injection.
 * Verifies that the container can properly inject dependencies based on their generic parameters.
 */
public class GenericDependencyInjectionTest {

    private SingletonObjectRegistryImpl registry;

    @BeforeEach
    public void setup() {
        this.registry = new SingletonObjectRegistryImpl();
    }

    @Test
    public void testGenericDependencyInjection() throws Exception {
        // Create and register all required components
        UserRepository userRepository = new UserRepository();
        ProductRepository productRepository = new ProductRepository();
        
        this.registry.registerSingleton(UserRepository.class, userRepository);
        this.registry.registerSingleton(ProductRepository.class, productRepository);
        
        // Create service instances with injected dependencies
        UserService userService = new UserService(userRepository);
        ProductService productService = new ProductService(productRepository);
        CombinedService combinedService = new CombinedService(userRepository, productRepository);
        
        this.registry.registerSingleton(UserService.class, userService);
        this.registry.registerSingleton(ProductService.class, productService);
        this.registry.registerSingleton(CombinedService.class, combinedService);

        // Retrieve services from registry
        UserService retrievedUserService = (UserService) this.registry.getSingleton(UserService.class);
        ProductService retrievedProductService = (ProductService) this.registry.getSingleton(ProductService.class);
        CombinedService retrievedCombinedService = (CombinedService) this.registry.getSingleton(CombinedService.class);
        
        // Verify services were retrieved correctly
        assertNotNull(retrievedUserService, "User service should not be null");
        assertNotNull(retrievedProductService, "Product service should not be null");
        assertNotNull(retrievedCombinedService, "Combined service should not be null");
        
        // Test UserService repository injection
        assertNotNull(retrievedUserService.getRepository(), "User repository should be injected");
        assertInstanceOf(UserRepository.class, retrievedUserService.getRepository(), "Should be UserRepository instance");
        assertEquals("user-1", retrievedUserService.getRepository().findById("1").getId(), "Should return correct user ID");
        
        // Test ProductService repository injection
        assertNotNull(retrievedProductService.getRepository(), "Product repository should be injected");
        assertInstanceOf(ProductRepository.class, retrievedProductService.getRepository(), "Should be ProductRepository instance");
        assertEquals("product-2", retrievedProductService.getRepository().findById("2").getId(), "Should return correct product ID");
        
        // Test CombinedService has both repositories with correct types
        assertNotNull(retrievedCombinedService.getUserRepository(), "User repository should be injected");
        assertNotNull(retrievedCombinedService.getProductRepository(), "Product repository should be injected");
        assertInstanceOf(UserRepository.class, retrievedCombinedService.getUserRepository(), "Should be UserRepository instance");
        assertInstanceOf(ProductRepository.class, retrievedCombinedService.getProductRepository(), "Should be ProductRepository instance");
        
        // Test TypeDescriptor functionality
        Field userRepoField = UserService.class.getDeclaredField("repository");
        TypeDescriptor userRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(userRepoField);
        assertEquals(Repository.class, userRepoTypeDesc.getRawType(), "Raw type should be Repository");
        assertEquals(User.class.getName(), userRepoTypeDesc.getGenericTypes()[0].getTypeName(), "Generic type should be User");
        
        Field productRepoField = ProductService.class.getDeclaredField("repository");
        TypeDescriptor productRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(productRepoField);
        assertEquals(Repository.class, productRepoTypeDesc.getRawType(), "Raw type should be Repository");
        assertEquals(Product.class.getName(), productRepoTypeDesc.getGenericTypes()[0].getTypeName(), "Generic type should be Product");
    }
    
    // Base entity class for all domain objects
    @Getter
    @RequiredArgsConstructor
    public static class Entity {
        private final String id;
    }
    
    // User entity extending the base entity
    public static class User extends Entity {
        public User(String id) {
            super("user-" + id);
        }
    }
    
    // Product entity extending the base entity
    public static class Product extends Entity {
        public Product(String id) {
            super("product-" + id);
        }
    }
    
    // Generic repository interface for any entity type
    public interface Repository<T extends Entity> {
        T findById(String id);
    }
    
    // User-specific repository implementation
    @InjectableComponent
    public static class UserRepository implements Repository<User> {
        @Override
        public User findById(String id) {
            return new User(id);
        }
    }
    
    // Product-specific repository implementation
    @InjectableComponent
    public static class ProductRepository implements Repository<Product> {
        @Override
        public Product findById(String id) {
            return new Product(id);
        }
    }
    
    // Service for user operations with injected user repository
    @Getter
    @InjectableComponent
    @RequiredArgsConstructor
    public static class UserService {
        private final Repository<User> repository;

        public User getUser(String id) {
            return repository.findById(id);
        }
    }
    
    // Service for product operations with injected product repository
    @Getter
    @InjectableComponent
    @RequiredArgsConstructor
    public static class ProductService {
        private final Repository<Product> repository;

        public Product getProduct(String id) {
            return repository.findById(id);
        }
    }
    
    // Service that requires both repositories with different generic parameters
    @Getter
    @InjectableComponent
    @RequiredArgsConstructor
    public static class CombinedService {
        private final Repository<User> userRepository;
        private final Repository<Product> productRepository;
    }
} 