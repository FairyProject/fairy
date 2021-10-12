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

package org.fairy.bukkit.util.text;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.fairy.locale.Locales;
import org.fairy.util.CC;

public interface IText {

    static IText of(String text) {
        return new NormalText(text);
    }

    static IText ofLocale(String locale) {
        return new LocaleText(locale);
    }

    String get(Player player);

    @RequiredArgsConstructor
    class NormalText implements IText {

        private final String text;

        @Override
        public String get(Player player) {
            return CC.translate(this.text);
        }
    }

    @RequiredArgsConstructor
    class LocaleText implements IText {

        private final String localeKey;

        @Override
        public String get(Player player) {
            return Locales.translate(player, this.localeKey);
        }
    }

}
