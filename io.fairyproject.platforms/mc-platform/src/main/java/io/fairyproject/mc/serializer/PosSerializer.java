package io.fairyproject.mc.serializer;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.object.Obj;
import io.fairyproject.mc.util.Position;

@Obj
public class PosSerializer implements ObjectSerializer<Position, String> {
    @Override
    public String serialize(Position input) {
        return input.toString();
    }

    @Override
    public Position deserialize(String output) {
        return Position.fromString(output);
    }

    @Override
    public Class<Position> inputClass() {
        return Position.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
