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

package io.fairyproject.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonChain
{
    private final JsonObject json;

    public JsonChain() {
        this.json = new JsonObject();
    }

    public JsonChain addProperty(final String property, final String value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain addProperty(final String property, final Object value) {
        this.json.addProperty(property, value.toString());
        return this;
    }

    public JsonChain addProperty(final String property, final Number value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain addProperty(final String property, final Boolean value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain addProperty(final String property, final Character value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain add(final String property, final JsonElement element) {
        this.json.add(property, element);
        return this;
    }

    public JsonObject get() {
        return this.json;
    }
}
