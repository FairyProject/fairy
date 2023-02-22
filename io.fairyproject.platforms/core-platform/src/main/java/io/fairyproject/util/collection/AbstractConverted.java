/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.util.collection;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents an object that transform elements of type VInner to type VOuter and back again.
 *
 * @credit ProtocolLib
 *
 * @param <VInner> - the first type.
 * @param <VOuter> - the second type.
 */
public abstract class AbstractConverted<VInner, VOuter> {
    // Inner conversion
    private Function<VOuter, VInner> innerConverter = new Function<VOuter, VInner>() {
        @Override
        public VInner apply(@Nullable VOuter param) {
            return toInner(param);
        }
    };

    // Outer conversion
    private Function<VInner, VOuter> outerConverter = new Function<VInner, VOuter>() {
        @Override
        public VOuter apply(@Nullable VInner param) {
            return toOuter(param);
        }
    };

    /**
     * Convert a value from the inner map to the outer visible map.
     * @param inner - the inner value.
     * @return The outer value.
     */
    protected abstract VOuter toOuter(VInner inner);

    /**
     * Convert a value from the outer map to the internal inner map.
     * @param outer - the outer value.
     * @return The inner value.
     */
    protected abstract VInner toInner(VOuter outer);

    /**
     * Retrieve a function delegate that converts outer objects to inner objects.
     * @return A function delegate.
     */
    protected Function<VOuter, VInner> getInnerConverter() {
        return innerConverter;
    }

    /**
     * Retrieve a function delegate that converts inner objects to outer objects.
     * @return A function delegate.
     */
    protected Function<VInner, VOuter> getOuterConverter() {
        return outerConverter;
    }
}