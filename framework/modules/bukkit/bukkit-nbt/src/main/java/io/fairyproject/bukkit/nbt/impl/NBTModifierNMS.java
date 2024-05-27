package io.fairyproject.bukkit.nbt.impl;

import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;
import io.fairyproject.bukkit.nbt.nms.NBTEditor;

@Deprecated
public class NBTModifierNMS implements NBTModifier {
    @Override
    public boolean has(Object holder, NBTKey key) {
        return NBTEditor.contains(holder, (Object[]) key.getKeys());
    }

    @Override
    public boolean getBoolean(Object holder, NBTKey key) {
        return NBTEditor.getBoolean(holder, (Object[]) key.getKeys());
    }

    @Override
    public long getLong(Object holder, NBTKey key) {
        return NBTEditor.getLong(holder, (Object[]) key.getKeys());
    }

    @Override
    public int getInt(Object holder, NBTKey key) {
        return NBTEditor.getInt(holder, (Object[]) key.getKeys());
    }

    @Override
    public short getShort(Object holder, NBTKey key) {
        return NBTEditor.getShort(holder, (Object[]) key.getKeys());
    }

    @Override
    public double getDouble(Object holder, NBTKey key) {
        return NBTEditor.getDouble(holder, (Object[]) key.getKeys());
    }

    @Override
    public float getFloat(Object holder, NBTKey key) {
        return NBTEditor.getFloat(holder, (Object[]) key.getKeys());
    }

    @Override
    public String getString(Object holder, NBTKey key) {
        return NBTEditor.getString(holder, (Object[]) key.getKeys());
    }

    @Override
    public <T> T setTag(T holder, NBTKey key, Object value) {
        return NBTEditor.set(holder, value, (Object[]) key.getKeys());
    }
}
