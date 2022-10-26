package io.fairyproject.bukkit.util.items.util;

import com.google.common.collect.ImmutableList;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.OBCClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.filter.FilterUnit;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@ApiStatus.Internal
@UtilityClass
@SuppressWarnings({"ResultOfMethodCallIgnored", "LambdaCanBeMethodReference"})
public class ItemBuilderUtil {

    private static Field GAME_PROFILE_FIELD;
    private static MethodWrapper<?> GET_PROFILE_ENTITY_HUMAN_METHOD;

    private static final BiConsumer<SkullMeta, Player> SET_SKULL_WITH_PLAYER = FilterUnit.<BiConsumer<SkullMeta, Player>>create()
            // modern 1.12+
            .add(FilterUnit.Item.<BiConsumer<SkullMeta, Player>>create((skullMeta, player) -> skullMeta.setOwningPlayer(player))
                    .predicate(FilterUnit.test(t -> SkullMeta.class.getDeclaredMethod("setOwningPlayer", OfflinePlayer.class), NoSuchMethodException.class)))
            // legacy
            .add((skullMeta, player) -> {
                if (GAME_PROFILE_FIELD == null) {
                    ThrowingRunnable.sneaky(() -> {
                        Class<?> entityHumanClass = new NMSClassResolver().resolve("world.entity.player.EntityHuman", "EntityHuman");
                        Class<?> craftMetaSkullClass = new OBCClassResolver().resolve("inventory.CraftMetaSkull");

                        final Field field = craftMetaSkullClass.getDeclaredField("profile");
                        AccessUtil.setAccessible(field);
                        GAME_PROFILE_FIELD = field;

                        GET_PROFILE_ENTITY_HUMAN_METHOD = new MethodResolver(entityHumanClass).resolve(MinecraftReflection.GAME_PROFILE_TYPE, 0);
                    }).run();
                }

                Object handle = MinecraftReflection.getHandleSilent(player);
                Object gameProfile = GET_PROFILE_ENTITY_HUMAN_METHOD.invokeSilent(handle);
                try {
                    GAME_PROFILE_FIELD.set(skullMeta, gameProfile);
                } catch (IllegalAccessException e) {
                    SneakyThrowUtil.sneakyThrow(e);
                }
            })
            .find()
            .orElseThrow(() -> new IllegalStateException("No valid set skull with player can be found."));

    private static final TriConsumer<ItemMeta, Locale, Component> SET_DISPLAY_NAME = FilterUnit.<TriConsumer<ItemMeta, Locale, Component>>create()
            // modern 1.16+
            .add(FilterUnit.Item.<TriConsumer<ItemMeta, Locale, Component>>create((itemMeta, locale, component) -> {
                        BungeeComponentSerializer serializer = BungeeComponentSerializer.get();
                        @NotNull BaseComponent[] baseComponents = serializer.serialize(component);
                        itemMeta.setDisplayNameComponent(baseComponents);
                    })
                    .predicate(FilterUnit.test(t -> ItemMeta.class.getDeclaredMethod("setDisplayNameComponent", BaseComponent[].class), NoSuchMethodException.class)))
            // legacy
            .add((itemMeta, locale, component) -> itemMeta.setDisplayName(MCAdventure.asLegacyString(component, locale)))
            .find()
            .orElseThrow(() -> new IllegalStateException("No valid set display name can be found."));

    private static final TriConsumer<ItemMeta, Locale, List<Component>> SET_LORE = FilterUnit.<TriConsumer<ItemMeta, Locale, List<Component>>>create()
            // modern 1.16+
            .add(FilterUnit.Item.<TriConsumer<ItemMeta, Locale, List<Component>>>create((itemMeta, locale, components) -> {
                        BungeeComponentSerializer serializer = BungeeComponentSerializer.get();
                        List<BaseComponent[]> baseComponents = components.stream()
                                .map(serializer::serialize)
                                .collect(Collectors.toList());
                        itemMeta.setLoreComponents(baseComponents);
                    })
                    .predicate(FilterUnit.test(t -> ItemMeta.class.getDeclaredMethod("setLoreComponents", List.class), NoSuchMethodException.class)))
            // legacy
            .add((itemMeta, locale, components) -> itemMeta.setLore(components.stream()
                    .map(component -> MCAdventure.asItemString(component, locale)) // how to handle locale?
                    .collect(Collectors.toList())))
            .find()
            .orElseThrow(() -> new IllegalStateException("No valid set lore can be found."));

    public static void setSkullWithPlayer(SkullMeta skullMeta, Player player) {
        SET_SKULL_WITH_PLAYER.accept(skullMeta, player);
    }

    public static void setDisplayName(ItemMeta itemMeta, Locale locale, Component component) {
        SET_DISPLAY_NAME.accept(itemMeta, locale, component);
    }

    public static void setLore(ItemMeta itemMeta, Locale locale, Iterable<Component> components) {
        SET_LORE.accept(itemMeta, locale, ImmutableList.copyOf(components));
    }

    private interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

}
