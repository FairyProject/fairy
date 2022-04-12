package io.fairyproject.mc.entity;

import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;

public abstract class EntityDataSerializer<T> {

    public static EntityDataSerializer<Double> DOUBLE = new EntityDataSerializer<Double>() {
        @Override
        public Double read(FriendlyByteBuf byteBuf) {
            return byteBuf.readDouble();
        }

        @Override
        public void write(FriendlyByteBuf byteBuf, Double obj) {
            byteBuf.writeDouble(obj);
        }
    };

    public T read(FriendlyByteBuf byteBuf) {
        throw new UnsupportedOperationException();
    }

    public void write(FriendlyByteBuf byteBuf, T obj) {
        throw new UnsupportedOperationException();
    }

}
