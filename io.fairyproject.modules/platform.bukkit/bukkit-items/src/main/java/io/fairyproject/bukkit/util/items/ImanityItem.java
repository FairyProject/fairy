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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.fairyproject.FairyPlatform;
import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;
import io.fairyproject.bukkit.util.items.behaviour.ItemBehaviour;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.PlaceholderEntry;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.metadata.MetadataMapProxy;
import io.fairyproject.util.StringUtil;
import io.fairyproject.util.terminable.Terminable;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
@JsonSerialize(using = ImanityItem.Serializer.class)
@JsonDeserialize(using = ImanityItem.Deserializer.class)
public final class ImanityItem implements Terminable, MetadataMapProxy {

    private static final Map<String, ImanityItem> NAME_TO_ITEMS = new ConcurrentHashMap<>();
    private static final Map<Plugin, List<ImanityItem>> PLUGIN_TO_ITEMS = new ConcurrentHashMap<>();
    private static final AtomicInteger UNNAMED_ITEM_COUNTER = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger(ImanityItem.class);
    private static final NBTKey KEY = NBTKey.create("imanity", "item", "id");

    private Plugin plugin;

    private String id;
    private boolean submitted;
    private ItemBuilder itemBuilder;
    private Component displayName;
    private Component displayLore;

    private final List<ItemBehaviour> behaviours = new ArrayList<>();
    private final MetadataMap metadataMap;

    public static ImanityItemBuilder builder(String id) {
        final Plugin plugin = ImanityItemBuilder.findPlugin(4);
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

        if (!NBTModifier.get().has(itemStack, KEY)) {
            return null;
        }

        return NBTModifier.get().getString(itemStack, KEY);
    }

    @Deprecated
    public ImanityItem() {
        this.metadataMap = MetadataMap.create();
    }

    private ImanityItem(Plugin plugin,
                        String id,
                        ItemBuilder itemBuilder,
                        String displayName,
                        String displayLore,
                        List<ItemBehaviour> behaviours,
                        List<PlaceholderEntry> displayNamePlaceholders,
                        List<PlaceholderEntry> displayLorePlaceholders,
                        MetadataMap metadataMap) {
        this(plugin,
                id,
                itemBuilder,
                Component.translatable(displayName),
                Component.translatable(displayLore),
                behaviours,
                displayNamePlaceholders,
                displayLorePlaceholders,
                metadataMap);
    }

    protected ImanityItem(Plugin plugin,
                          String id,
                          ItemBuilder itemBuilder,
                          Component displayName,
                          Component displayLore,
                          List<ItemBehaviour> behaviours,
                          List<PlaceholderEntry> displayNamePlaceholders,
                          List<PlaceholderEntry> displayLorePlaceholders,
                          MetadataMap metadataMap) {
        this.plugin = plugin;
        this.id = id;
        this.itemBuilder = itemBuilder;
        this.displayName = displayName;
        this.displayLore = displayLore;
        this.behaviours.addAll(behaviours);
        this.metadataMap = metadataMap;
    }

    @Deprecated
    public Object getMetadata(String key) {
        return this.metadataMap.getOrNull(MetadataKey.create(key, Object.class));
    }

    public ImanityItem item(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ImanityItem displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public ImanityItem displayLore(Component displayLore) {
        this.displayLore = displayLore;
        return this;
    }

    @Deprecated
    public ImanityItem displayNameLocale(Component locale) {
        this.displayName = locale;
        return this;
    }

    @Deprecated
    public ImanityItem displayLoreLocale(Component locale) {
        this.displayLore = locale;
        return this;
    }

    public ImanityItem addBehaviour(ItemBehaviour behaviour) {
        this.behaviours.add(behaviour);
        return this;
    }

    @Deprecated
    public ImanityItem metadata(String key, Object object) {
        this.metadataMap.put(MetadataKey.create(key, Object.class), object);
        return this;
    }

    public ImanityItem submit() {
        if (this.getItemBuilder() == null) {
            throw new IllegalArgumentException("No Item registered!");
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
        return this;
    }

    public Material getType() {
        return this.itemBuilder.getType();
    }

    @Override
    public void close() {
        this.unregister();
    }

    @Override
    public boolean isClosed() {
        return !this.submitted;
    }

    public void unregister() {
        for (ItemBehaviour behaviour : this.behaviours) {
            behaviour.unregister();
        }

        NAME_TO_ITEMS.remove(this.id);
        this.submitted = false;
    }

    public ItemStack get(Player player) {
        return this.get(MCPlayer.from(player));
    }

    @Deprecated
    public ItemStack build(Player player) {
        return this.get(MCPlayer.from(player));
    }

    public ItemStack get(MCPlayer player) {
        if (this.getItemBuilder() == null) {
            throw new IllegalArgumentException("No Item registered!");
        }
        Locale locale = player.getLocale();

        ItemBuilder itemBuilder = this.itemBuilder.clone();
        if (displayName != null) {
            String name = MCAdventure.asItemString(displayName, locale);
            itemBuilder.name(name);
        }

        if (displayLore != null) {
            String lore = MCAdventure.asItemString(displayLore, locale);
            itemBuilder.lore(StringUtil.separateLines(lore, "\n"));
        }

        if (!this.submitted) {
            return itemBuilder.build();
        }
        return itemBuilder.tag(KEY, this.id).build();
    }

    @Override
    public MetadataMap getMetadataMap() {
        return this.metadataMap;
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