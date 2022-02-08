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

package io.fairyproject.locale;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.*;
import net.kyori.adventure.translation.Translator;

import java.util.Locale;

@Component
@ServiceDependency(value = LocaleService.class, type = ServiceDependencyType.SUB_DISABLE)
public class LocaleSerializer implements ObjectSerializer<Locale, String> {

    @Autowired
    private LocaleService localeService;

    @Override
    public String serialize(Locale input) {
        return input.toString();
    }

    @Override
    public Locale deserialize(String output) {
        return Translator.parseLocale(output);
    }

    @Override
    public Class<Locale> inputClass() {
        return Locale.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
