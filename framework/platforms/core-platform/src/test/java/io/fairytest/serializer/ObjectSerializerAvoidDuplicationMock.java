package io.fairytest.serializer;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.serializer.AvoidDuplicate;

@AvoidDuplicate
public class ObjectSerializerAvoidDuplicationMock<I, O> implements ObjectSerializer<I, O> {

    private final Class<I> i;
    private final Class<O> o;

    public ObjectSerializerAvoidDuplicationMock(Class<I> i, Class<O> o) {
        this.i = i;
        this.o = o;
    }

    @Override
    public O serialize(I input) {
        return null;
    }

    @Override
    public I deserialize(O output) {
        return null;
    }

    @Override
    public Class<I> inputClass() {
        return this.i;
    }

    @Override
    public Class<O> outputClass() {
        return this.o;
    }

}
