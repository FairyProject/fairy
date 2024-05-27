package io.fairyproject.bukkit.nbt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NBTKey {

    private String[] keys;

    public static NBTKey create(String... keys) {
        final NBTKey nbtKey = new NBTKey();
        nbtKey.setKeys(keys);
        return nbtKey;
    }

    @Override
    public String toString() {
        return String.join(".", keys);
    }

}
