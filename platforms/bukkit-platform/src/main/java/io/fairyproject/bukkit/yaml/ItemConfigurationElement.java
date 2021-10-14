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

package io.fairyproject.bukkit.yaml;

import io.fairyproject.bean.Autowired;
import io.fairyproject.bukkit.util.items.ImanityItem;
import io.fairyproject.bukkit.util.items.ItemBuilder;
import io.fairyproject.config.annotation.ConfigurationElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.fairy.locale.Locale;
import org.fairy.locale.LocaleService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConfigurationElement
public class ItemConfigurationElement {

    @Autowired
    private static Optional<LocaleService> LOCALE_HANDLER;

    private Material material = Material.AIR;
    private short data = 0;

    private String identityName = "none";

    private Map<String, String> displayNameLocales = new HashMap<>();
    private Map<String, String> displayLoreLocales = new HashMap<>();

    private transient ImanityItem item;

    public void register() {

        LOCALE_HANDLER.ifPresent(localeHandler -> {
            for (Map.Entry<String, String> entry : this.displayNameLocales.entrySet()) {

                Locale locale = localeHandler.getOrRegister(entry.getKey());
                locale.registerEntry(this.getLocaleName(), entry.getValue());

            }

            for (Map.Entry<String, String> entry : this.displayLoreLocales.entrySet()) {

                Locale locale = localeHandler.getOrRegister(entry.getKey());
                locale.registerEntry(this.getLocaleLore(), entry.getValue());

            }
        });

        this.item = ImanityItem.builder(this.identityName)
                .item(new ItemBuilder(this.material)
                        .durability(this.data))
                .displayNameLocale(this.getLocaleName())
                .displayLoreLocale(this.getLocaleLore())
                .build();

    }

    public String getLocaleName() {
        return "item." + this.identityName + ".name";
    }
    public String getLocaleLore() {
        return "item." + this.identityName + ".lore";
    }

    public ImanityItem toImanityItem(Player player) {
        if (this.item == null) {
            this.register();
        }
        return this.item;
    }

}
