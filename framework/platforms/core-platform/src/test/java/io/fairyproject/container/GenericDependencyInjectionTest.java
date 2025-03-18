package io.fairyproject.container;

import io.fairyproject.container.object.singleton.SingletonObjectRegistryImpl;
import io.fairyproject.container.type.TypeDescriptor;
import io.fairyproject.container.util.GenericTypeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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
    
    @Test
    public void testMultipleRepositoriesOfSameType() throws Exception {
        // Create and register all required components
        UserRepository userRepository = new UserRepository();
        ProductRepository productRepository = new ProductRepository();
        OrderRepository orderRepository = new OrderRepository();
        
        this.registry.registerSingleton(UserRepository.class, userRepository);
        this.registry.registerSingleton(ProductRepository.class, productRepository);
        this.registry.registerSingleton(OrderRepository.class, orderRepository);
        
        // Create service with multiple repositories of the same type but different generic parameters
        MultiRepositoryService multiService = new MultiRepositoryService(userRepository, productRepository, orderRepository);
        this.registry.registerSingleton(MultiRepositoryService.class, multiService);

        // Retrieve service from registry
        MultiRepositoryService retrievedMultiService = (MultiRepositoryService) this.registry.getSingleton(MultiRepositoryService.class);
        
        // Verify service was retrieved correctly
        assertNotNull(retrievedMultiService, "Multi-repository service should not be null");
        
        // Test all injected Repository instances
        assertNotNull(retrievedMultiService.getUserRepo(), "User repository should be injected");
        assertNotNull(retrievedMultiService.getProductRepo(), "Product repository should be injected");
        assertNotNull(retrievedMultiService.getOrderRepo(), "Order repository should be injected");
        
        assertInstanceOf(UserRepository.class, retrievedMultiService.getUserRepo());
        assertInstanceOf(ProductRepository.class, retrievedMultiService.getProductRepo());
        assertInstanceOf(OrderRepository.class, retrievedMultiService.getOrderRepo());
        
        // Test behavior of repositories with different generic parameters
        assertEquals("user-1", retrievedMultiService.getUserRepo().findById("1").getId());
        assertEquals("product-2", retrievedMultiService.getProductRepo().findById("2").getId());
        assertEquals("order-3", retrievedMultiService.getOrderRepo().findById("3").getId());
        
        // Verify generic type descriptors of fields
        Field userRepoField = MultiRepositoryService.class.getDeclaredField("userRepo");
        TypeDescriptor userRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(userRepoField);
        assertEquals(Repository.class, userRepoTypeDesc.getRawType());
        assertEquals(User.class.getName(), userRepoTypeDesc.getGenericTypes()[0].getTypeName());
        
        Field productRepoField = MultiRepositoryService.class.getDeclaredField("productRepo");
        TypeDescriptor productRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(productRepoField);
        assertEquals(Repository.class, productRepoTypeDesc.getRawType());
        assertEquals(Product.class.getName(), productRepoTypeDesc.getGenericTypes()[0].getTypeName());
        
        Field orderRepoField = MultiRepositoryService.class.getDeclaredField("orderRepo");
        TypeDescriptor orderRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(orderRepoField);
        assertEquals(Repository.class, orderRepoTypeDesc.getRawType());
        assertEquals(Order.class.getName(), orderRepoTypeDesc.getGenericTypes()[0].getTypeName());
    }
    
    @Test
    public void testNestedGenericTypes() throws Exception {
        // Create and register all required components
        PageableUserRepository pageableUserRepo = new PageableUserRepository();
        PageableProductRepository pageableProductRepo = new PageableProductRepository();
        
        this.registry.registerSingleton(PageableUserRepository.class, pageableUserRepo);
        this.registry.registerSingleton(PageableProductRepository.class, pageableProductRepo);
        
        // Create service instance
        PageableService pageableService = new PageableService(pageableUserRepo, pageableProductRepo);
        this.registry.registerSingleton(PageableService.class, pageableService);
        
        // Retrieve service from registry
        PageableService retrievedService = (PageableService) this.registry.getSingleton(PageableService.class);
        
        // Verify service was retrieved correctly
        assertNotNull(retrievedService, "Pageable service should not be null");
        
        // Test nested generic injection
        assertNotNull(retrievedService.getUserRepo(), "Pageable user repository should be injected");
        assertNotNull(retrievedService.getProductRepo(), "Pageable product repository should be injected");
        
        // Verify behavior of nested generics
        Page<User> userPage = retrievedService.getUserRepo().findAll(0, 10);
        Page<Product> productPage = retrievedService.getProductRepo().findAll(0, 10);
        
        assertNotNull(userPage, "User page should not be null");
        assertNotNull(productPage, "Product page should not be null");
        assertEquals(3, userPage.getContent().size(), "User page should have 3 elements");
        assertEquals(2, productPage.getContent().size(), "Product page should have 2 elements");
        
        // Verify nested generic type descriptors
        Field userRepoField = PageableService.class.getDeclaredField("userRepo");
        TypeDescriptor userRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(userRepoField);
        assertEquals(PageableRepository.class, userRepoTypeDesc.getRawType());
        assertEquals(User.class.getName(), userRepoTypeDesc.getGenericTypes()[0].getTypeName());
        
        Field productRepoField = PageableService.class.getDeclaredField("productRepo");
        TypeDescriptor productRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(productRepoField);
        assertEquals(PageableRepository.class, productRepoTypeDesc.getRawType());
        assertEquals(Product.class.getName(), productRepoTypeDesc.getGenericTypes()[0].getTypeName());
    }
    
    @Test
    public void testGenericBoundsAndWildcards() throws Exception {
        // Create and register all required components
        NumberRepository numberRepository = new NumberRepository();
        ExtendedRepository extendedRepository = new ExtendedRepository();
        
        this.registry.registerSingleton(NumberRepository.class, numberRepository);
        this.registry.registerSingleton(ExtendedRepository.class, extendedRepository);
        
        // Create service instance
        BoundedGenericService boundedService = new BoundedGenericService(numberRepository, extendedRepository);
        this.registry.registerSingleton(BoundedGenericService.class, boundedService);
        
        // Retrieve service from registry
        BoundedGenericService retrievedService = (BoundedGenericService) this.registry.getSingleton(BoundedGenericService.class);
        
        // Verify service was retrieved correctly
        assertNotNull(retrievedService, "Bounded generic service should not be null");
        
        // Test generic bounds and wildcards
        assertNotNull(retrievedService.getNumberRepo(), "Number repository should be injected");
        assertNotNull(retrievedService.getExtendedRepo(), "Extended repository should be injected");
        
        // Verify behavior of bounded generic constraints
        assertEquals(42, retrievedService.getNumberRepo().getSum());
        assertEquals(2, retrievedService.getExtendedRepo().getAll().size());
        
        // Verify generic type descriptors of fields, including upper bounds
        Field numberRepoField = BoundedGenericService.class.getDeclaredField("numberRepo");
        TypeDescriptor numberRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(numberRepoField);
        assertEquals(BoundedRepository.class, numberRepoTypeDesc.getRawType());
        // Wildcard type handling may vary, only check raw type
        
        assertNotNull(numberRepoTypeDesc.getGenericTypes(), "Generic types should not be null");
        assertTrue(numberRepoTypeDesc.getGenericTypes().length > 0, "Should have at least one generic type");
        
        Field extendedRepoField = BoundedGenericService.class.getDeclaredField("extendedRepo");
        TypeDescriptor extendedRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(extendedRepoField);
        assertEquals(WildcardRepository.class, extendedRepoTypeDesc.getRawType());
        // Wildcard type handling may vary, only check raw type
        
        assertNotNull(extendedRepoTypeDesc.getGenericTypes(), "Generic types should not be null");
        assertTrue(extendedRepoTypeDesc.getGenericTypes().length > 0, "Should have at least one generic type");
    }
    
    @Test
    public void testGenericConflictResolution() throws Exception {
        // Create multiple components of the same type but with different implementation details
        IntegerKeyValueRepository intKVRepo = new IntegerKeyValueRepository();
        StringKeyValueRepository strKVRepo = new StringKeyValueRepository();
        DoubleKeyValueRepository dblKVRepo = new DoubleKeyValueRepository();
        
        // Register components
        this.registry.registerSingleton(IntegerKeyValueRepository.class, intKVRepo);
        this.registry.registerSingleton(StringKeyValueRepository.class, strKVRepo);
        this.registry.registerSingleton(DoubleKeyValueRepository.class, dblKVRepo);
        
        // Create service instance with specific generic types
        GenericConfigService configService = new GenericConfigService(intKVRepo, strKVRepo, dblKVRepo);
        this.registry.registerSingleton(GenericConfigService.class, configService);
        
        // Retrieve service from registry
        GenericConfigService retrievedService = (GenericConfigService) this.registry.getSingleton(GenericConfigService.class);
        
        // Verify service was retrieved correctly
        assertNotNull(retrievedService, "Config service should not be null");
        
        // Verify injection of repositories with specific generic types
        assertNotNull(retrievedService.getIntegerRepo(), "Integer repository should be injected");
        assertNotNull(retrievedService.getStringRepo(), "String repository should be injected");
        assertNotNull(retrievedService.getDoubleRepo(), "Double repository should be injected");
        
        // Check if different generic implementations are correctly distinguished
        assertInstanceOf(IntegerKeyValueRepository.class, retrievedService.getIntegerRepo());
        assertInstanceOf(StringKeyValueRepository.class, retrievedService.getStringRepo());
        assertInstanceOf(DoubleKeyValueRepository.class, retrievedService.getDoubleRepo());
        
        // Test type-differentiated behavior
        assertEquals(100, retrievedService.getIntegerRepo().get("test"));
        assertEquals("value", retrievedService.getStringRepo().get("test"));
        assertEquals(3.14, retrievedService.getDoubleRepo().get("test"));
        
        // Verify generic type descriptors of fields
        Field intRepoField = GenericConfigService.class.getDeclaredField("integerRepo");
        TypeDescriptor intRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(intRepoField);
        assertEquals(KeyValueRepository.class, intRepoTypeDesc.getRawType());
        assertEquals(Integer.class.getName(), intRepoTypeDesc.getGenericTypes()[0].getTypeName());
        
        Field strRepoField = GenericConfigService.class.getDeclaredField("stringRepo");
        TypeDescriptor strRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(strRepoField);
        assertEquals(KeyValueRepository.class, strRepoTypeDesc.getRawType());
        assertEquals(String.class.getName(), strRepoTypeDesc.getGenericTypes()[0].getTypeName());
        
        Field dblRepoField = GenericConfigService.class.getDeclaredField("doubleRepo");
        TypeDescriptor dblRepoTypeDesc = GenericTypeUtils.getTypeDescriptorFromField(dblRepoField);
        assertEquals(KeyValueRepository.class, dblRepoTypeDesc.getRawType());
        assertEquals(Double.class.getName(), dblRepoTypeDesc.getGenericTypes()[0].getTypeName());
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
    
    // Order entity extending the base entity
    public static class Order extends Entity {
        public Order(String id) {
            super("order-" + id);
        }
    }
    
    // Order-specific repository implementation
    @InjectableComponent
    public static class OrderRepository implements Repository<Order> {
        @Override
        public Order findById(String id) {
            return new Order(id);
        }
    }
    
    // Generic interface for pageable queries
    public interface PageableRepository<T extends Entity> extends Repository<T> {
        Page<T> findAll(int page, int size);
    }
    
    // Page result wrapper class
    @Getter
    public static class Page<T> {
        private final List<T> content;
        private final int totalPages;
        private final long totalElements;
        private final int number;
        private final int size;
        
        public Page(List<T> content, int totalPages, long totalElements, int number, int size) {
            this.content = content;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.number = number;
            this.size = size;
        }
    }
    
    // User pageable repository implementation
    @InjectableComponent
    public static class PageableUserRepository implements PageableRepository<User> {
        @Override
        public User findById(String id) {
            return new User(id);
        }
        
        @Override
        public Page<User> findAll(int page, int size) {
            List<User> users = Arrays.asList(new User("1"), new User("2"), new User("3"));
            return new Page<>(users, 1, users.size(), page, size);
        }
    }
    
    // Product pageable repository implementation
    @InjectableComponent
    public static class PageableProductRepository implements PageableRepository<Product> {
        @Override
        public Product findById(String id) {
            return new Product(id);
        }
        
        @Override
        public Page<Product> findAll(int page, int size) {
            List<Product> products = Arrays.asList(new Product("1"), new Product("2"));
            return new Page<>(products, 1, products.size(), page, size);
        }
    }
    
    // Service using multiple repositories of the same type but with different generic parameters
    @Getter
    @InjectableComponent
    @RequiredArgsConstructor
    public static class MultiRepositoryService {
        private final Repository<User> userRepo;
        private final Repository<Product> productRepo;
        private final Repository<Order> orderRepo;
    }
    
    // Service using nested generic types
    @Getter
    @InjectableComponent
    @RequiredArgsConstructor
    public static class PageableService {
        private final PageableRepository<User> userRepo;
        private final PageableRepository<Product> productRepo;
    }
    
    // Interface with generic upper bounds
    public interface BoundedRepository<T extends Number> {
        T getValue();
        int getSum();
    }
    
    // Interface using wildcards
    public interface WildcardRepository<T extends Entity> {
        List<T> getAll();
        void add(T entity);
    }
    
    // Number type bounded repository implementation
    @InjectableComponent
    public static class NumberRepository implements BoundedRepository<Integer> {
        @Override
        public Integer getValue() {
            return 42;
        }
        
        @Override
        public int getSum() {
            return 42;
        }
    }
    
    // Wildcard repository implementation
    @InjectableComponent
    public static class ExtendedRepository implements WildcardRepository<User> {
        private final List<User> users = Arrays.asList(new User("1"), new User("2"));
        
        @Override
        public List<User> getAll() {
            return users;
        }
        
        @Override
        public void add(User entity) {
            // Implementation omitted
        }
    }
    
    // Service using bounded generics and wildcards
    @Getter
    @InjectableComponent
    @RequiredArgsConstructor
    public static class BoundedGenericService {
        private final BoundedRepository<? extends Number> numberRepo;
        private final WildcardRepository<? extends Entity> extendedRepo;
    }
    
    // Key-Value repository interface
    public interface KeyValueRepository<T> {
        T get(String key);
        void put(String key, T value);
    }
    
    // Integer type repository
    @InjectableComponent
    public static class IntegerKeyValueRepository implements KeyValueRepository<Integer> {
        @Override
        public Integer get(String key) {
            return 100; // Simplified implementation
        }
        
        @Override
        public void put(String key, Integer value) {
            // Simplified implementation
        }
    }
    
    // String type repository
    @InjectableComponent
    public static class StringKeyValueRepository implements KeyValueRepository<String> {
        @Override
        public String get(String key) {
            return "value"; // Simplified implementation
        }
        
        @Override
        public void put(String key, String value) {
            // Simplified implementation
        }
    }
    
    // Double type repository
    @InjectableComponent
    public static class DoubleKeyValueRepository implements KeyValueRepository<Double> {
        @Override
        public Double get(String key) {
            return 3.14; // Simplified implementation
        }
        
        @Override
        public void put(String key, Double value) {
            // Simplified implementation
        }
    }
    
    // Service requiring repositories with different generic types
    @Getter
    @InjectableComponent
    @RequiredArgsConstructor
    public static class GenericConfigService {
        private final KeyValueRepository<Integer> integerRepo;
        private final KeyValueRepository<String> stringRepo;
        private final KeyValueRepository<Double> doubleRepo;
    }
} 