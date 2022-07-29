package io.fairyproject.bukkit.nbt.impl;

import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NBTModifierMock implements NBTModifier {

    private final Map<Object, Map<NBTKey, Object>> cache = new ConcurrentHashMap<>(4);

    private Map<NBTKey, Object> getMap(Object holder) {
        return this.cache.computeIfAbsent(holder, i -> new ConcurrentHashMap<>());
    }

    public Object getTag(Object holder, NBTKey key) {
        return this.getMap(holder).get(key);
    }

    @Override
    public boolean has(Object holder, NBTKey key) {
        return this.getMap(holder).containsKey(key);
    }

    @Override
    public boolean getBoolean(Object holder, NBTKey key) {
        return (boolean) this.getTag(holder, key);
    }

    @Override
    public long getLong(Object holder, NBTKey key) {
        return (long) this.getTag(holder, key);
    }

    @Override
    public int getInt(Object holder, NBTKey key) {
        return (int) this.getTag(holder, key);
    }

    @Override
    public short getShort(Object holder, NBTKey key) {
        return (short) this.getTag(holder, key);
    }

    @Override
    public double getDouble(Object holder, NBTKey key) {
        return (double) this.getTag(holder, key);
    }

    @Override
    public float getFloat(Object holder, NBTKey key) {
        return (float) this.getTag(holder, key);
    }

    @Override
    public String getString(Object holder, NBTKey key) {
        return (String) this.getTag(holder, key);
    }

    @Override
    public <T> T setTag(T holder, NBTKey key, Object value) {
        this.getMap(holder).put(key, value);
        return holder;
    }
}
