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

import io.fairyproject.bean.Autowired;
import io.fairyproject.bean.BeanHolder;
import io.fairyproject.mc.PlaceholderEntry;
import io.fairyproject.util.RV;
import io.fairyproject.util.StringUtil;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Static extension for Locale translation
 */
@UtilityClass
public class Locales {

    @Autowired
    private BeanHolder<LocaleService> LOCALE_SERVICE;

    public String translate(UUID uuid, String key) {
        return LOCALE_SERVICE.supplyOrNull(localeService -> localeService.translate(uuid, key));
    }

    public String translate(UUID uuid, String key, RV... rvs) {
        return LOCALE_SERVICE.supplyOrNull(localeService -> StringUtil.replace(localeService.translate(uuid, key), rvs));
    }

    public void setLocale(UUID uuid, Locale locale) {
        LOCALE_SERVICE.runOrNull(localeService -> localeService.setLocale(uuid, locale));
    }

    public void setLocale(UUID uuid, String localeName) {
        LOCALE_SERVICE.runOrNull(localeService -> localeService.setLocale(uuid, localeName));
    }

    public Locale getLocale(UUID uuid) {
        return LOCALE_SERVICE.supplyOrNull(localeService -> localeService.getLocale(uuid));
    }

    public <Player> String translate(Player player, String key) {
        return LOCALE_SERVICE.supplyOrNull(localeService -> localeService.translate(player, key));
    }

    public <Player> String translate(Player player, String key, RV... rvs) {
        return LOCALE_SERVICE.supplyOrNull(localeService -> StringUtil.replace(localeService.translate(player, key), rvs));
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

    public PlaceholderEntry entry(String placeholder, String localeEntry) {
        return PlaceholderEntry.entry(placeholder, mcPlayer -> LOCALE_SERVICE.supplyOrNull(localeService -> localeService.translate(mcPlayer, localeEntry)));
    }

    public LocaleMessage message(String localeEntry) {
        return new LocaleMessage(localeEntry);
    }

}
