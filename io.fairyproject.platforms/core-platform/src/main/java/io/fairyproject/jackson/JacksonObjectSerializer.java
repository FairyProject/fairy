package io.fairyproject.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.fairyproject.ObjectSerializer;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class JacksonObjectSerializer extends StdSerializer {

    private final ObjectSerializer serializer;

    @SuppressWarnings("unchecked")
    public JacksonObjectSerializer(ObjectSerializer serializer) {
        super(serializer.inputClass());
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeObject(serializer.serialize(value));
    }
}
