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

package io.fairyproject.mc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class PlaceholderEntry {

    private final String target;
    private final Function<MCPlayer, String> replacement;

    public PlaceholderEntry(String target, String replacement) {
        this.target = target;
        this.replacement = player -> replacement;
    }

    public PlaceholderEntry(String target, Object replacement) {
        this(target, replacement.toString());
    }

    public String getReplacement(MCPlayer player) {
        return this.replacement.apply(player);
    }

    public static PlaceholderEntry entry(final String target, final String replacement) {
        return new PlaceholderEntry(target, replacement);
    }

    public static PlaceholderEntry entry(final String target, final Object replacement) {
        return new PlaceholderEntry(target, replacement);
    }

    public static PlaceholderEntry entry(String target, Function<MCPlayer, String> replacement) {
        return new PlaceholderEntry(target, replacement);
    }

}
