package io.fairyproject.container.object.singleton;

import io.fairyproject.container.object.LifeCycle;
import io.fairyproject.container.type.TypeDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingletonObjectRegistryTypeDescriptorTest {

    private SingletonObjectRegistryImpl registry;

    @BeforeEach
    void setUp() {
        this.registry = new SingletonObjectRegistryImpl();
    }

    @Test
    public void testRegisterSingletonWithTypeDescriptor() {
        TypeDescriptor typeDescriptor = new TypeDescriptor(TestService.class);
        TestService instance = new TestService();

        this.registry.registerSingleton(typeDescriptor, instance);

        assertTrue(this.registry.containsSingleton(typeDescriptor));
        assertEquals(instance, this.registry.getSingleton(typeDescriptor));
    }

    @Test
    public void testRegisterSingletonWithGenericTypeDescriptor() {
        TypeDescriptor listStringTypeDescriptor = new TypeDescriptor(List.class, new Type[]{String.class});
        List<String> stringList = Arrays.asList("test1", "test2");

        this.registry.registerSingleton(listStringTypeDescriptor, stringList);

        assertTrue(this.registry.containsSingleton(listStringTypeDescriptor));
        assertEquals(stringList, this.registry.getSingleton(listStringTypeDescriptor));
    }

    @Test
    public void testGetSingletonTypeDescriptors() {
        TypeDescriptor typeDescriptor1 = new TypeDescriptor(TestService.class);
        TypeDescriptor typeDescriptor2 = new TypeDescriptor(List.class, new Type[]{String.class});

        TestService service = new TestService();
        List<String> stringList = Arrays.asList("test");

        this.registry.registerSingleton(typeDescriptor1, service);
        this.registry.registerSingleton(typeDescriptor2, stringList);

        Set<TypeDescriptor> typeDescriptors = this.registry.getSingletonTypeDescriptors();
        assertEquals(2, typeDescriptors.size());
        assertTrue(typeDescriptors.contains(typeDescriptor1));
        assertTrue(typeDescriptors.contains(typeDescriptor2));
    }

    @Test
    public void testRemoveSingletonWithTypeDescriptor() {
        TypeDescriptor typeDescriptor = new TypeDescriptor(TestService.class);
        TestService instance = new TestService();

        this.registry.registerSingleton(typeDescriptor, instance);
        assertTrue(this.registry.containsSingleton(typeDescriptor));

        this.registry.removeSingleton(typeDescriptor);
        assertFalse(this.registry.containsSingleton(typeDescriptor));
        assertNull(this.registry.getSingleton(typeDescriptor));
    }

    @Test
    public void testLifeCycleWithTypeDescriptor() {
        TypeDescriptor typeDescriptor = new TypeDescriptor(TestService.class);

        this.registry.setSingletonLifeCycle(typeDescriptor, LifeCycle.POST_INIT);
        assertEquals(LifeCycle.POST_INIT, this.registry.getSingletonLifeCycle(typeDescriptor));
    }

    @Test
    public void testLifeCycleDefaultValue() {
        TypeDescriptor typeDescriptor = new TypeDescriptor(TestService.class);
        assertEquals(LifeCycle.NONE, this.registry.getSingletonLifeCycle(typeDescriptor));
    }

    @Test
    public void testBackwardCompatibilityWithClassBasedMethods() {
        TestService instance = new TestService();
        this.registry.registerSingleton(TestService.class, instance);

        TypeDescriptor typeDescriptor = new TypeDescriptor(TestService.class);
        assertEquals(instance, this.registry.getSingleton(typeDescriptor));
        assertTrue(this.registry.containsSingleton(typeDescriptor));
    }

    @Test
    public void testBackwardCompatibilityWithTypeDescriptorBasedMethods() {
        TypeDescriptor typeDescriptor = new TypeDescriptor(TestService.class);
        TestService instance = new TestService();
        this.registry.registerSingleton(typeDescriptor, instance);

        assertEquals(instance, this.registry.getSingleton(TestService.class));
        assertTrue(this.registry.containsSingleton(TestService.class));
    }

    @Test
    public void testRegisterSingletonTwiceWithTypeDescriptorShouldThrowException() {
        TypeDescriptor typeDescriptor = new TypeDescriptor(TestService.class);
        TestService instance1 = new TestService();
        TestService instance2 = new TestService();

        this.registry.registerSingleton(typeDescriptor, instance1);

        assertThrows(IllegalStateException.class, () -> this.registry.registerSingleton(typeDescriptor, instance2));
    }

    @Test
    public void testGenericTypeCompatibility() {
        TypeDescriptor listStringTypeDescriptor = new TypeDescriptor(List.class, new Type[]{String.class});
        TypeDescriptor listIntegerTypeDescriptor = new TypeDescriptor(List.class, new Type[]{Integer.class});

        List<String> stringList = Arrays.asList("test");
        List<Integer> integerList = Arrays.asList(1, 2, 3);

        this.registry.registerSingleton(listStringTypeDescriptor, stringList);
        this.registry.registerSingleton(listIntegerTypeDescriptor, integerList);

        assertEquals(stringList, this.registry.getSingleton(listStringTypeDescriptor));
        assertEquals(integerList, this.registry.getSingleton(listIntegerTypeDescriptor));

        assertNotEquals(this.registry.getSingleton(listStringTypeDescriptor),
                this.registry.getSingleton(listIntegerTypeDescriptor));
    }

    private static class TestService {

    }

}