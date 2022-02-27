package io.fairyproject.mc.protocol.packet.translate;

public interface Translator<F, T> {
    T transform(final F from);
}
