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

package io.fairyproject.bukkit.util.items;

import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.bukkit.util.BukkitUtil;
import io.fairyproject.bukkit.util.items.behaviour.ItemBehaviour;
import io.fairyproject.mc.PlaceholderEntry;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ImanityItemBuilder {

    public static void example(Player tester) {
        // new item builder instance
        final ItemStack item = new ImanityItemBuilder("test:example-item-bed")
                // the item reference, you can use .item(new ItemBuilder()) instead for more flexibility
                .item(XMaterial.WHITE_WOOL)
                // locale name (don't use it if you don't have locale)
                .displayNameLocale("item.bed.name")
                // locale lore (don't use it if you don't have locale)
                .displayLoreLocale("item.bed.lore")
                // add behaviour on block place and print a "you placed the bed" message
                .addBehaviour(ItemBehaviour.blockPlace((player, itemStack, block, event) -> {
                    player.sendMessage("you placed the wool");
                }))
                // add behaviour that listening to custom event
                .addBehaviour(ItemBehaviour.ofEvent(PlayerDeathEvent.class, (event, itemBehaviour) -> {
                    Player player = event.getEntity();
                    ItemStack itemStack = player.getItemInHand();

                    // check if the item matches and other filters
                    if (!itemBehaviour.matches(player, itemStack)) {
                        return;
                    }

                    player.sendMessage("YOU DEAD WITH A WOOL");
                }))
                // finally, build it
                .build()
                .get(tester);

        tester.getInventory().addItem(item);
    }

    private final Plugin plugin;
    private final String id;

    private ItemBuilder itemBuilder;
    private Component displayName;
    private Component displayLore;
    private final List<ItemBehaviour> behaviours = new ArrayList<>();
    private final List<PlaceholderEntry> displayNamePlaceholders = new ArrayList<>();
    private final List<PlaceholderEntry> displayLorePlaceholders = new ArrayList<>();

    private final Map<String, Object> metadata = new HashMap<>();

    public ImanityItemBuilder(String id) {
        this(id, findPlugin(4));
    }

    public ImanityItemBuilder(@NonNull String id, Plugin plugin) {
        this.id = id;
        this.plugin = plugin;
    }

    public static Plugin findPlugin(int depth) {
        return BukkitUtil.getCurrentPlugin(depth + 1);
    }

    public ImanityItemBuilder item(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ImanityItemBuilder item(ItemStack itemStack) {
        this.itemBuilder = new ItemBuilder(itemStack);
        return this;
    }

    public ImanityItemBuilder item(XMaterial material) {
        this.itemBuilder = new ItemBuilder(material);
        return this;
    }

    public ImanityItemBuilder displayNameLocale(String locale) {
        this.displayName = Component.translatable(locale);
        return this;
    }

    public ImanityItemBuilder displayLoreLocale(String locale) {
        this.displayLore = Component.translatable(locale);
        return this;
    }

    public ImanityItemBuilder displayName(Component component) {
        this.displayName = component;
        return this;
    }

    public ImanityItemBuilder displayLore(Component component) {
        this.displayLore = component;
        return this;
    }

    @Deprecated
    public ImanityItemBuilder displayNameLocale(Component component) {
        return this.displayName(component);
    }

    @Deprecated
    public ImanityItemBuilder displayLoreLocale(Component component) {
        return this.displayLore(component);
    }

    @Deprecated
    public ImanityItemBuilder appendNameReplace(String target, Function<Player, String> replacement) {
        this.displayNamePlaceholders.add(PlaceholderEntry.entry(target, replacement));
        return this;
    }

    @Deprecated
    public ImanityItemBuilder appendLoreReplace(String target, Function<Player, String> replacement) {
        this.displayLorePlaceholders.add(PlaceholderEntry.entry(target, replacement));
        return this;
    }

    public ImanityItemBuilder addBehaviour(ItemBehaviour behaviour) {
        this.behaviours.add(behaviour);
        return this;
    }

    public ImanityItemBuilder metadata(String key, Object object) {
        this.metadata.put(key, object);
        return this;
    }

    public ImanityItem build() {
        return this.buildNoTag().submit();
    }

    public ImanityItem buildNoTag() {
        return new ImanityItem(
                this.plugin,
                this.id,
                this.itemBuilder,
                this.displayName,
                this.displayLore,
                this.behaviours,
                this.displayNamePlaceholders,
                this.displayLorePlaceholders,
                this.metadata
        );
    }

}
