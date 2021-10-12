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

package org.fairy.locale;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.fairy.util.Utility;

import java.util.List;
import java.util.Map;

public class Locale {

    private final Char2ObjectOpenHashMap<Map<String, String>> translateEntries = new Char2ObjectOpenHashMap<>();

    @Getter
    private final String name;

    protected Locale(String name) {
        this.name = name;
    }

    public void registerEntry(String key, String value) {
        char c = this.getEntry(key);

        Map<String, String> subEntries;
        if (this.translateEntries.containsKey(c)) {
            subEntries = this.translateEntries.get(c);
        } else {
            subEntries = new Object2ObjectOpenHashMap<>();
            this.translateEntries.put(c, subEntries);
        }

        subEntries.put(key, value);
    }

    public void registerEntries(String path, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("locale")) {
                continue;
            }

            if (entry.getValue() instanceof List) {
                List list = (List) entry.getValue();
                this.registerEntry(path + entry.getKey(), (String[]) list.stream().map(Object::toString).toArray(String[]::new));
            } else if (entry.getValue() instanceof Map) {
                this.registerEntries(path + entry.getKey() + ".", (Map<String, Object>) entry.getValue());
            } else {
                this.registerEntry(path + entry.getKey(), entry.getValue().toString());
            }
        }
    }

    public void registerEntry(String key, Iterable<String> strings) {
        this.registerEntry(key, Utility.joinToString(strings, "\n"));
    }

    public void registerEntry(String key, String[] strings) {
        this.registerEntry(key, Utility.joinToString(strings, "\n"));
    }

    public void unregisterEntry(String key) {
        char c = this.getEntry(key);

        if (translateEntries.containsKey(c)) {
            Map<String, String> subEntries = this.translateEntries.get(c);

            subEntries.remove(key);

            if (subEntries.isEmpty()) {
                translateEntries.remove(c);
            }
        }
    }

    public String get(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        char c = this.getEntry(key);

        if (this.translateEntries.containsKey(c)) {
            Map<String, String> subEntries = this.translateEntries.get(c);

            if (subEntries.containsKey(key)) {
                return subEntries.get(key);
            }
            return key;
        }

        return key;
    }

    public char getEntry(String key) {
        return key.charAt(0);
    }
}
