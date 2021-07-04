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

import org.fairy.bean.Autowired;
import org.fairy.bean.BeanHolder;
import org.fairy.util.Utility;
import org.fairy.util.entry.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocaleBuilder {

    @Autowired
    private static BeanHolder<LocaleService> LOCALE_HANDLER;

    private String name;
    private List<Entry<String, String>> entries = new ArrayList<>();

    public LocaleBuilder name(String name) {
        this.name = name;
        return this;
    }

    public LocaleBuilder entry(String key, String value) {
        Entry<String, String> entry = new Entry<>(key, value);
        this.entries.add(entry);
        return this;
    }

    public LocaleBuilder entry(String key, Iterable<String> value) {
        return this.entry(key, Utility.joinToString(value, "\n"));
    }

    public LocaleBuilder entry(String key, String[] value) {
        return this.entry(key, Utility.joinToString(value, "\n"));
    }

    public LocaleBuilder entries(String... entries) {

        if (entries.length % 2 != 0) {
            throw new IllegalStateException("The entries is not even-numbered!");
        }

        for (int i = 0; i < entries.length; i += 2) {
            this.entry(entries[i], entries[i + 1]);
        }

        return this;

    }

    public Locale build() {
        if (!LOCALE_HANDLER.contains()) {
            throw new IllegalStateException("Trying to register Locale while Locale is not enabled!");
        }
        Locale locale = LOCALE_HANDLER.getOrNull().getLocaleByName(name);
        for (Entry<String, String> entry : this.entries) {
            locale.registerEntry(entry.getKey(), entry.getValue());
        }
        return locale;
    }

}
