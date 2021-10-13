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

import org.fairy.mc.message.MCMessage;
import org.fairy.mc.MCPlayer;
import org.fairy.mc.PlaceholderEntry;
import org.fairy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LocaleMessage implements MCMessage {

    private final String localeName;
    private final List<PlaceholderEntry> replaceValues;

    public LocaleMessage(String localeName) {
        this.localeName = localeName;
        this.replaceValues = new ArrayList<>();
    }

    public LocaleMessage appendReplacement(String target, String replacement) {
        this.replaceValues.add(PlaceholderEntry.entry(target, replacement));
        return this;
    }

    public LocaleMessage appendReplacement(String target, Object replacement) {
        this.replaceValues.add(PlaceholderEntry.entry(target, replacement));
        return this;
    }

    public LocaleMessage appendReplacement(String target, Function<MCPlayer, String> replacement) {
        this.replaceValues.add(PlaceholderEntry.entry(target, replacement));
        return this;
    }

    public LocaleMessage appendLocaleReplacement(String target, String localeReplacement) {
        this.replaceValues.add(Locales.entry(target, localeReplacement));
        return this;
    }

    @Override
    public String get(MCPlayer player) {
        String result = Locales.translate(player, this.localeName);

        for (PlaceholderEntry rv : this.replaceValues) {
            result = StringUtil.replace(result, rv.getTarget(), rv.getReplacement(player));
        }

        return result;
    }
}
