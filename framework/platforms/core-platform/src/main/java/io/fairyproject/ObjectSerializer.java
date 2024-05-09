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

package io.fairyproject;

/**
 * The serializer that serializes type from I to O.
 * You can register through annotating {@link io.fairyproject.container.object.Obj} or {@link io.fairyproject.container.SerializerFactory#registerSerializer(ObjectSerializer)}
 * @param <I> the input type
 * @param <O> the output type
 */
public interface ObjectSerializer<I, O> {

    /**
     * Serialize an instance from input type to output type.
     * @param input the input instance
     * @return the output instance
     */
    O serialize(I input);

    /**
     * Deserialize an instance from output type to input type.
     * @param output the output instance
     * @return the input instance
     */
    I deserialize(O output);

    /**
     * The input type
     * @return type
     */
    Class<I> inputClass();

    /**
     * The output type
     * @return type
     */
    Class<O> outputClass();

}
