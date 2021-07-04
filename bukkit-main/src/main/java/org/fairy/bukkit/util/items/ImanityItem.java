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

package org.fairy.bukkit.util.items;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.fairy.bukkit.util.LocaleRV;
import org.fairy.bukkit.util.nms.NBTEditor;
import org.fairy.locale.Locales;
import org.fairy.bukkit.util.items.behaviour.ItemBehaviour;
import org.fairy.util.StringUtil;
import org.fairy.util.terminable.Terminable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
@JsonSerialize(using = ImanityItem.Serializer.class)
@JsonDeserialize(using = ImanityItem.Deserializer.class)
public final class ImanityItem implements Terminable {

    private static final Map<String, ImanityItem> NAME_TO_ITEMS = new ConcurrentHashMap<>();
    private static final Map<Plugin, List<ImanityItem>> PLUGIN_TO_ITEMS = new ConcurrentHashMap<>();
    private static final AtomicInteger UNNAMED_ITEM_COUNTER = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();

    public static ImanityItemBuilder builder(String id) {
        final Plugin plugin = ImanityItemBuilder.findPlugin(4);
        System.out.println(plugin.getName());
        return new ImanityItemBuilder(id, plugin);
    }

    public static ImanityItem getItem(String id) {
        return NAME_TO_ITEMS.get(id);
    }

    @Nullable
    public static ImanityItem getItemFromBukkit(ItemStack itemStack) {
        String value = getItemKeyFromBukkit(itemStack);

        if (value == null) {
            return null;
        }

        return NAME_TO_ITEMS.get(value);
    }

    public static String getItemKeyFromBukkit(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }

        if (!NBTEditor.contains(itemStack, "imanity", "item", "id")) {
            return null;
        }

        return NBTEditor.getString(itemStack, "imanity", "item", "id");
    }

    private Plugin plugin;

    private String id;
    private boolean submitted;
    private ItemBuilder itemBuilder;
    private String displayNameLocale;
    private String displayLoreLocale;

    private ItemCallback clickCallback;
    private ItemPlaceCallback placeCallback;

    private final List<ItemBehaviour> behaviours = new ArrayList<>();
    private final List<LocaleRV> displayNamePlaceholders = new ArrayList<>();
    private final List<LocaleRV> displayLorePlaceholders = new ArrayList<>();

    private final Map<String, Object> metadata = new HashMap<>();

    @Deprecated
    public ImanityItem() {
    }

    protected ImanityItem(Plugin plugin,
                          String id,
                          ItemBuilder itemBuilder,
                          String displayNameLocale,
                          String displayLoreLocale,
                          List<ItemBehaviour> behaviours,
                          List<LocaleRV> displayNamePlaceholders,
                          List<LocaleRV> displayLorePlaceholders,
                          Map<String, Object> metadata) {
        this.plugin = plugin;
        this.id = id;
        this.itemBuilder = itemBuilder;
        this.displayNameLocale = displayNameLocale;
        this.displayLoreLocale = displayLoreLocale;
        this.behaviours.addAll(behaviours);
        this.displayNamePlaceholders.addAll(displayNamePlaceholders);
        this.displayLorePlaceholders.addAll(displayLorePlaceholders);
        this.metadata.putAll(metadata);
    }

    public  Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    public ImanityItem item(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ImanityItem displayNameLocale(String locale) {
        this.displayNameLocale = locale;
        return this;
    }

    public ImanityItem displayLoreLocale(String locale) {
        this.displayLoreLocale = locale;
        return this;
    }

    public ImanityItem appendNameReplace(String target, Function<Player, String> replacement) {
        this.displayNamePlaceholders.add(LocaleRV.o(target, replacement));
        return this;
    }

    public ImanityItem appendLoreReplace(String target, Function<Player, String> replacement) {
        this.displayLorePlaceholders.add(LocaleRV.o(target, replacement));
        return this;
    }

    @Deprecated
    public ImanityItem callback(ItemCallback callback) {
        this.clickCallback = callback;
        return this;
    }

    @Deprecated
    public ImanityItem placeCallback(ItemPlaceCallback placeCallback) {
        this.placeCallback = placeCallback;
        return this;
    }

    public ImanityItem addBehaviour(ItemBehaviour behaviour) {
        this.behaviours.add(behaviour);
        return this;
    }

    public ImanityItem metadata(String key, Object object) {
        this.metadata.put(key, object);
        return this;
    }

    public ImanityItem submit() {

        if (this.getItemBuilder() == null) {
            throw new IllegalArgumentException("No Item registered!");
        }

        if (this.placeCallback != null && !this.getItemBuilder().getType().isBlock()) {
            throw new IllegalArgumentException("Registering ItemPlaceCallback but the item isn't a block!");
        }

        if (this.id == null) {
            this.id = "unnamed-item:" + UNNAMED_ITEM_COUNTER.getAndIncrement();
            LOGGER.warn("The Item doesn't have an id! (outdated?)", new Throwable());
        }

        if (NAME_TO_ITEMS.containsKey(this.id)) {
            throw new IllegalArgumentException("The item with name " + this.id + " already exists!");
        }

        NAME_TO_ITEMS.put(this.id, this);
        for (ItemBehaviour behaviour : this.behaviours) {
            behaviour.init0(this);
        }
        this.submitted = true;

        return this;

    }

    public Material getType() {
        return this.itemBuilder.getType();
    }

    @Override
    public void close() throws Exception {
        this.unregister();
    }

    public void unregister() {
        for (ItemBehaviour behaviour : this.behaviours) {
            behaviour.unregister();
        }

        NAME_TO_ITEMS.remove(this.id);
    }

    public ItemStack get(Player player) {
        return this.build(player);
    }

    @Deprecated
    public ItemStack build(Player receiver) {
        if (this.getItemBuilder() == null) {
            throw new IllegalArgumentException("No Item registered!");
        }

        ItemBuilder itemBuilder = this.itemBuilder.clone();

        if (displayNameLocale != null) {
            String name = Locales.translate(receiver, displayNameLocale);
            for (LocaleRV rv : this.displayNamePlaceholders) {
                name = StringUtil.replace(name, rv.getTarget(), rv.getReplacement(receiver));
            }

            itemBuilder.name(name);
        }

        if (displayLoreLocale != null) {
            String lore = Locales.translate(receiver, displayLoreLocale);
            for (LocaleRV rv : this.displayLorePlaceholders) {
                lore = StringUtil.replace(lore, rv.getTarget(), rv.getReplacement(receiver));
            }

            itemBuilder.lore(StringUtil.separateLines(lore, "\n"));

        }

        if (!this.submitted) {
            return itemBuilder.build();
        }
        return itemBuilder
                .tag(this.id, "imanity", "item", "id")
                .build();
    }

    public static class Serializer extends StdSerializer<ImanityItem> {

        protected Serializer() {
            super(ImanityItem.class);
        }

        @Override
        public void serialize(ImanityItem item, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(item.id);
        }
    }

    public static class Deserializer extends StdDeserializer<ImanityItem> {

        protected Deserializer() {
            super(ImanityItem.class);
        }

        @Override
        public ImanityItem deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return ImanityItem.getItem(jsonParser.getValueAsString());
        }
    }
}