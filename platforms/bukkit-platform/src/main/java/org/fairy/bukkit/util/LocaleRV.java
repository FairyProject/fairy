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

package org.fairy.bukkit.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.fairy.locale.Locales;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class LocaleRV {

    private final String target;
    private final Function<Player, String> replacement;

    public LocaleRV(String target, String replacement) {
        this.target = target;
        this.replacement = player -> replacement;
    }

    public LocaleRV(String target, Object replacement) {
        this(target, replacement.toString());
    }

    public String getReplacement(Player player) {
        return this.replacement.apply(player);
    }

    public static LocaleRV o(final String target, final String replacement) {
        return new LocaleRV(target, replacement);
    }

    public static LocaleRV o(final String target, final Object replacement) {
        return new LocaleRV(target, replacement);
    }

    public static LocaleRV o(String target, Function<Player, String> replacement) {
        return new LocaleRV(target, replacement);
    }

    public static LocaleRV oT(String target, String locale) {
        return new LocaleRV(target, player -> Locales.translate(player, locale));
    }

}
