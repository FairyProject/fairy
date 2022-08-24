package io.fairyproject.mc.serializer;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.object.Obj;
import io.fairyproject.mc.util.Pos;

@Obj
public class PosSerializer implements ObjectSerializer<Pos, String> {
    @Override
    public String serialize(Pos input) {
        return input.toString();
    }

    @Override
    public Pos deserialize(String output) {
        return Pos.fromString(output);
    }

    @Override
    public Class<Pos> inputClass() {
        return Pos.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
