package io.fairytest.serializer;

import io.fairyproject.ObjectSerializer;

public class ObjectSerializerStringStringMock implements ObjectSerializer<String, String> {
    @Override
    public String serialize(String input) {
        return input + "hello";
    }

    @Override
    public String deserialize(String output) {
        return output.substring(0, output.length() - 5);
    }

    @Override
    public Class<String> inputClass() {
        return String.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
