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

package io.fairyproject.bukkit.locale;

import io.fairyproject.bean.Autowired;
import io.fairyproject.bean.Component;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.command.annotation.CommandHolder;
import io.fairyproject.locale.LocaleService;
import io.fairyproject.bukkit.command.event.BukkitCommandEvent;
import io.fairyproject.locale.LocaleData;

@Component
public class LocaleCommand implements CommandHolder {

    @Autowired
    private BukkitLocaleStorage localeStorage;

    @Autowired
    private LocaleService localeService;

    @Command(names = "bruh")
    public void taiwan(BukkitCommandEvent event) {
        final LocaleData localeData = this.localeStorage.find(event.getPlayer().getUniqueId());
        localeData.setLocale(this.localeService.getOrRegister("zh_tw"));
        this.localeStorage.save(event.getPlayer().getUniqueId());
        System.out.println(localeData.getLocale());
    }

    @Command(names = "hiya")
    public void tai1wan(BukkitCommandEvent event) {
        final LocaleData localeData = this.localeStorage.find(event.getPlayer().getUniqueId());
        System.out.println(localeData.getLocale());
    }

}
