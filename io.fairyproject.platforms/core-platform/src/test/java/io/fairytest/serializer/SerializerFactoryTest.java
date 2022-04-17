package io.fairytest.serializer;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.Containers;
import io.fairyproject.container.SerializerFactory;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializerFactoryTest extends JUnitJupiterBase {

    @AfterEach
    public void afterEach() {
        final SerializerFactory serializerFactory = Containers.get(SerializerFactory.class);

        serializerFactory.unregisterSerializer(TestKey.class);
    }

    @Test
    public void findSerializer() {
        final SerializerFactory serializerFactory = Containers.get(SerializerFactory.class);

        ObjectSerializer<TestKey, TestKey> a = new ObjectSerializerAvoidDuplicationMock<>(TestKey.class, TestKey.class);
        serializerFactory.registerSerializer(a);
        Assertions.assertEquals(a, serializerFactory.findSerializer(TestKey.class));
    }

    @Test
    public void findSerializerNoRegister() {
        final SerializerFactory serializerFactory = Containers.get(SerializerFactory.class);

        Assertions.assertNull(serializerFactory.findSerializer(TestKey.class));
    }

    @Test
    public void serializerUnregisterNull() {
        final SerializerFactory serializerFactory = Containers.get(SerializerFactory.class);

        ObjectSerializer<TestKey, TestKey> a = new ObjectSerializerAvoidDuplicationMock<>(TestKey.class, TestKey.class);
        serializerFactory.registerSerializer(a);
        serializerFactory.unregisterSerializer(a);

        Assertions.assertNull(serializerFactory.findSerializer(TestKey.class));
    }

    @Test
    public void serializerKeyOrder() {
        final SerializerFactory serializerFactory = Containers.get(SerializerFactory.class);

        ObjectSerializer<TestKey, TestKey> a = new ObjectSerializerMock<>(TestKey.class, TestKey.class);
        ObjectSerializer<TestKey, TestKey> b = new ObjectSerializerMock<>(TestKey.class, TestKey.class);

        serializerFactory.registerSerializer(a);
        serializerFactory.registerSerializer(b);

        Assertions.assertEquals(a, serializerFactory.findSerializer(TestKey.class));
    }

    @Test
    public void serializerAvoidDuplication() {
        final SerializerFactory serializerFactory = Containers.get(SerializerFactory.class);

        ObjectSerializer<TestKey, TestKey> a = new ObjectSerializerAvoidDuplicationMock<>(TestKey.class, TestKey.class);
        ObjectSerializer<TestKey, TestKey> b = new ObjectSerializerAvoidDuplicationMock<>(TestKey.class, TestKey.class);

        serializerFactory.registerSerializer(a);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            serializerFactory.registerSerializer(b);
        });
    }

    @Test
    public void serializerUnregisterDuplication() {
        final SerializerFactory serializerFactory = Containers.get(SerializerFactory.class);

        ObjectSerializer<TestKey, TestKey> a = new ObjectSerializerAvoidDuplicationMock<>(TestKey.class, TestKey.class);
        ObjectSerializer<TestKey, TestKey> b = new ObjectSerializerAvoidDuplicationMock<>(TestKey.class, TestKey.class);

        serializerFactory.registerSerializer(a);
        Assertions.assertTrue(serializerFactory.unregisterSerializer(a));
        serializerFactory.registerSerializer(b);
        Assertions.assertEquals(b, serializerFactory.findSerializer(TestKey.class));
    }

    public static class TestKey { }

}
