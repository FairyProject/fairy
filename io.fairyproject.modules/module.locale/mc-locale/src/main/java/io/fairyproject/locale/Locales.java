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

import io.fairyproject.container.Autowired;
import io.fairyproject.container.ContainerHolder;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Static extension for Locale translation
 */
@UtilityClass
public class Locales {

    @Autowired
    private ContainerHolder<LocaleService> LOCALE_SERVICE;

    public void setLocale(UUID uuid, Locale locale) {
        LOCALE_SERVICE.runOrNull(localeService -> localeService.setLocale(uuid, locale));
    }

    public void setLocale(UUID uuid, String localeName) {
        LOCALE_SERVICE.runOrNull(localeService -> localeService.setLocale(uuid, localeName));
    }

    public Locale getLocale(UUID uuid) {
        return LOCALE_SERVICE.supplyOrNull(localeService -> localeService.getLocale(uuid));
    }

    public <Player> void setLocale(Player player, Locale locale) {
        LOCALE_SERVICE.runOrNull(localeService -> localeService.setLocale(player, locale));
    }

    public <Player> void setLocale(Player player, String localeName) {
        LOCALE_SERVICE.runOrNull(localeService -> localeService.setLocale(player, localeName));
    }

    public <Player> Locale getLocale(Player player) {
        return LOCALE_SERVICE.supplyOrNull(localeService -> localeService.getLocale(player));
    }

    public Component translate(String key) {
        return Component.translatable(key);
    }

}
