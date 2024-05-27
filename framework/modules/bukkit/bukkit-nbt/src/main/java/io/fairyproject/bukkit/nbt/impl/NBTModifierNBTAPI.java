package io.fairyproject.bukkit.nbt.impl;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class NBTModifierNBTAPI implements NBTModifier {

    private Object modify(Object holder, Consumer<ReadWriteNBT> consumer) {
        if (holder instanceof ItemStack) {
            ItemStack itemStack = ((ItemStack) holder).clone();
            NBT.modify(itemStack, consumer::accept);
            return itemStack;
        } else if (holder instanceof Entity) {
            NBT.modify((Entity) holder, consumer);
        } else if (holder instanceof BlockState) {
            NBT.modify((BlockState) holder, consumer);
        } else if (holder instanceof Block) {
            NBT.modify(((Block) holder).getState(), consumer);
        }
        return holder;
    }

    private ReadableNBT read(Object holder) {
        if (holder instanceof ItemStack) {
            return NBT.readNbt((ItemStack) holder);
        } else if (holder instanceof Entity) {
            return new NBTEntity((Entity) holder);
        } else if (holder instanceof BlockState) {
            return new NBTTileEntity((BlockState) holder);
        } else if (holder instanceof Block) {
            return new NBTTileEntity(((Block) holder).getState());
        }
        return null;
    }

    @Override
    public boolean has(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return false;
        return nbt.hasTag(key.toString());
    }

    @Override
    public boolean getBoolean(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return false;
        return nbt.getBoolean(key.toString());
    }

    @Override
    public long getLong(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return 0;
        return nbt.getLong(key.toString());
    }

    @Override
    public int getInt(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return 0;
        return nbt.getInteger(key.toString());
    }

    @Override
    public short getShort(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return 0;
        return nbt.getShort(key.toString());
    }

    @Override
    public double getDouble(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return 0;
        return nbt.getDouble(key.toString());
    }

    @Override
    public float getFloat(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return 0;
        return nbt.getFloat(key.toString());
    }

    @Override
    public String getString(Object holder, NBTKey key) {
        ReadableNBT nbt = read(holder);
        if (nbt == null)
            return null;
        return nbt.getString(key.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T setTag(T holder, NBTKey key, Object value) {
        return (T) modify(holder, nbt -> {
            if (value instanceof Integer) {
                nbt.setInteger(key.toString(), (Integer) value);
            } else if (value instanceof Long) {
                nbt.setLong(key.toString(), (Long) value);
            } else if (value instanceof Short) {
                nbt.setShort(key.toString(), (Short) value);
            } else if (value instanceof Double) {
                nbt.setDouble(key.toString(), (Double) value);
            } else if (value instanceof Float) {
                nbt.setFloat(key.toString(), (Float) value);
            } else if (value instanceof Boolean) {
                nbt.setBoolean(key.toString(), (Boolean) value);
            } else if (value instanceof String) {
                nbt.setString(key.toString(), (String) value);
            } else if (value instanceof ItemStack) {
                nbt.setItemStack(key.toString(), (ItemStack) value);
            } else if (value instanceof ItemStack[]) {
                nbt.setItemStackArray(key.toString(), (ItemStack[]) value);
            }
        });
    }
}
