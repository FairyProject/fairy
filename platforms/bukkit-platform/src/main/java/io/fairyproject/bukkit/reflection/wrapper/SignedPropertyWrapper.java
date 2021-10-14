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

package io.fairyproject.bukkit.reflection.wrapper;

import io.fairyproject.bukkit.reflection.wrapper.impl.SignedPropertyImplementation;
import lombok.Getter;

public class SignedPropertyWrapper extends WrapperAbstract {

    private static final SignedPropertyImplementation IMPLEMENTATION = SignedPropertyImplementation.getImplementation();

    @Getter
    private Object handle;

    public SignedPropertyWrapper(Object handle) {
        this.handle = handle;
    }

    public SignedPropertyWrapper(String name, String value, String signature) {
        this(IMPLEMENTATION.create(name, value, signature));
    }

    @Override
    public boolean exists() {
        return handle != null;
    }

    public String getName() {
        return IMPLEMENTATION.getName(handle);
    }

    public String getValue() {
        return IMPLEMENTATION.getValue(handle);
    }

    public String getSignature() {
        return IMPLEMENTATION.getSignature(handle);
    }

    public SignedPropertyWrapper withName(String name) {
        this.handle = IMPLEMENTATION.create(name, this.getValue(), this.getSignature());
        return this;
    }

    public SignedPropertyWrapper withValue(String value) {
        this.handle = IMPLEMENTATION.create(this.getName(), value, this.getSignature());
        return this;
    }

    public SignedPropertyWrapper withSignature(String signature) {
        this.handle = IMPLEMENTATION.create(this.getName(), this.getValue(), signature);
        return this;
    }
}
