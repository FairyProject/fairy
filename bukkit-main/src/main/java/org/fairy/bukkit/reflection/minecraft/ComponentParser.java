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

package org.fairy.bukkit.reflection.minecraft;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ComponentParser {
    private static Constructor readerConstructor;
    private static Method setLenient;
    private static Method getAdapter;
    private static Method read;

    private ComponentParser() {
    }

    public static Object deserialize(Object gson, Class<?> component, StringReader str) {
        try {
            JsonReader reader = new JsonReader(str);
            reader.setLenient(true);
            return ((Gson)gson).getAdapter(component).read(reader);
        } catch (IOException var4) {
            throw new RuntimeException("Failed to read JSON", var4);
        } catch (LinkageError var5) {
            return deserializeLegacy(gson, component, str);
        }
    }

    private static Object deserializeLegacy(Object gson, Class<?> component, StringReader str) {
        try {
            Object adapter;
            if (readerConstructor == null) {
                Class<?> readerClass = Class.forName("org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonReader");
                readerConstructor = readerClass.getDeclaredConstructor(Reader.class);
                readerConstructor.setAccessible(true);
                setLenient = readerClass.getDeclaredMethod("setLenient", Boolean.TYPE);
                setLenient.setAccessible(true);
                getAdapter = gson.getClass().getDeclaredMethod("getAdapter", Class.class);
                getAdapter.setAccessible(true);
                adapter = getAdapter.invoke(gson, component);
                read = adapter.getClass().getDeclaredMethod("read", readerClass);
                read.setAccessible(true);
            }

            Object reader = readerConstructor.newInstance(str);
            setLenient.invoke(reader, true);
            adapter = getAdapter.invoke(gson, component);
            return read.invoke(adapter, reader);
        } catch (ReflectiveOperationException var5) {
            throw new RuntimeException("Failed to read JSON", var5);
        }
    }
}
