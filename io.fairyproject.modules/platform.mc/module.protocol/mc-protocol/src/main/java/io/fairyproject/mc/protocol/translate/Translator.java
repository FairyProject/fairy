package io.fairyproject.mc.protocol.translate;

public interface Translator<F, T> {
    T transform(final F from);
}
