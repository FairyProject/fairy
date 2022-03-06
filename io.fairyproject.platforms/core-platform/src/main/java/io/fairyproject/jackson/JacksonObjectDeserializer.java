package io.fairyproject.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.fairyproject.ObjectSerializer;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class JacksonObjectDeserializer extends StdDeserializer {

    private final ObjectSerializer serializer;

    @SuppressWarnings("unchecked")
    public JacksonObjectDeserializer(ObjectSerializer serializer) {
        super(serializer.inputClass());
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        return serializer.deserialize(jsonParser.readValueAs(serializer.outputClass()));
    }

}
