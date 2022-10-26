package io.fairyproject.bukkit.util.items;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.util.items.impl.ItemBuilderImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface ItemBuilder extends Cloneable {

    static ItemBuilder of(XMaterial material) {
        return new ItemBuilderImpl(material);
    }

    static ItemBuilder of(Material material) {
        return new ItemBuilderImpl(XMaterial.matchXMaterial(material));
    }

    static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilderImpl(itemStack);
    }

    @Contract("_ -> this")
    default ItemBuilder name(Component component) {
        return name(Locale.ENGLISH, component);
    }

    @Contract("_ -> this")
    default ItemBuilder lores(Component... components) {
        return lores(Locale.ENGLISH, components);
    }

    @Contract("_ -> this")
    default ItemBuilder lores(Iterable<Component> component) {
        return lores(Locale.ENGLISH, component);
    }

    @Contract("_, _ -> this")
    ItemBuilder name(Locale locale, Component name);

    @Contract("_, _ -> this")
    ItemBuilder lores(Locale locale, Iterable<Component> lore);

    @Contract("_, _ -> this")
    ItemBuilder lores(Locale locale, Component... lore);

    @Contract("_ -> this")
    ItemBuilder name(String name);

    @Contract("_ -> this")
    ItemBuilder lore(Iterable<String> lore);

    @Contract("_ -> this")
    ItemBuilder lore(String... lore);

    @Contract("_ -> this")
    ItemBuilder amount(int amount);

    @Contract("_ -> this")
    ItemBuilder unbreakable(boolean unbreakable);

    @Contract("_ -> this")
    ItemBuilder durability(int durability);

    @Contract("_ -> this")
    ItemBuilder data(int data);

    @Contract("_, _ -> this")
    ItemBuilder enchantment(XEnchantment enchantment, int level);

    @Contract("_ -> this")
    ItemBuilder enchantment(XEnchantment enchantment);

    @Contract("_ -> this")
    ItemBuilder type(XMaterial material);

    @Contract(" -> this")
    ItemBuilder clearLore();

    @Contract(" -> this")
    ItemBuilder clearEnchantments();

    @Contract("_ -> this")
    ItemBuilder color(Color color);

    @Contract("_ -> this")
    ItemBuilder skull(String owner);

    @Contract("_ -> this")
    ItemBuilder skull(Player owner);

    @Contract(" -> this")
    ItemBuilder shiny();

    @Contract("_, _ -> this")
    @Deprecated
    ItemBuilder tag(Object value, String... key);

    @Contract("_, _ -> this")
    ItemBuilder tag(NBTKey key, Object value);

    @Contract("_ -> this")
    ItemBuilder itemFlag(ItemFlag itemFlag);

    @Contract("_ -> this")
    ItemBuilder removeItemFlag(ItemFlag itemFlag);

    @Contract("_ -> this")
    ItemBuilder editMeta(Consumer<ItemMeta> consumer);

    @Contract("_, _ -> this")
    <T extends ItemMeta> ItemBuilder editMeta(Class<T> metaClass, Consumer<T> consumer);

    @Contract("_ -> this")
    ItemBuilder editItemStack(Consumer<ItemStack> consumer);

    @Contract("_ -> this")
    ItemBuilder transformMeta(Function<ItemMeta, ItemMeta> function);

    @Contract("_, _ -> this")
    <T extends ItemMeta> ItemBuilder transformMeta(Class<T> metaClass, Function<T, T> function);

    @Contract("_ -> this")
    ItemBuilder transformItemStack(Function<ItemStack, ItemStack> function);

    @Contract(" -> new")
    ItemStack build();

    Material getType();
}
