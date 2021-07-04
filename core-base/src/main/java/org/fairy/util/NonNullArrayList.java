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

package org.fairy.util;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor
public class NonNullArrayList<E> extends ArrayList<E> {

    public NonNullArrayList(Collection<E> collection) {
        super(collection);

        for (E e : collection) {
            if (e == null) {
                throw new NullPointerException("The List shouldn't be added any null object!");
            }
        }
    }

    @Override
    public boolean add(E e) {
        if (e == null) {
            throw new NullPointerException("The List shouldn't be added any null object!");
        }
        return super.add(e);
    }

    @Override
    public void add(int i, E e) {
        if (e == null) {
            throw new NullPointerException("The List shouldn't be added any null object!");
        }
        super.add(i, e);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E e : collection) {
            if (e == null) {
                throw new NullPointerException("The List shouldn't be added any null object!");
            }
        }

        return super.addAll(collection);
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> collection) {
        for (E e : collection) {
            if (e == null) {
                throw new NullPointerException("The List shouldn't be added any null object!");
            }
        }

        return super.addAll(i, collection);
    }
}
