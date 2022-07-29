package io.fairyproject.bukkit.nbt;

import io.fairyproject.Debug;
import io.fairyproject.bukkit.nbt.impl.NBTModifierMock;
import io.fairyproject.bukkit.nbt.impl.NBTModifierNMS;

public interface NBTModifier {

    static NBTModifier get() {
        if (Companion.INSTANCE == null) {
            if (Debug.UNIT_TEST) {
                Companion.INSTANCE = new NBTModifierMock();
            } else {
                Companion.INSTANCE = new NBTModifierNMS();
            }
        }
        return Companion.INSTANCE;
    }

    boolean has(Object holder, NBTKey key);

    boolean getBoolean(Object holder, NBTKey key);

    long getLong(Object holder, NBTKey key);

    int getInt(Object holder, NBTKey key);

    short getShort(Object holder, NBTKey key);

    double getDouble(Object holder, NBTKey key);

    float getFloat(Object holder, NBTKey key);

    String getString(Object holder, NBTKey key);

    <T> T setTag(T holder, NBTKey key, Object value);

    class Companion {

        public static NBTModifier INSTANCE;

    }

}
