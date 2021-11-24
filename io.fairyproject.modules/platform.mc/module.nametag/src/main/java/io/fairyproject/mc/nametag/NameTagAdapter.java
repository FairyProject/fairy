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

package io.fairyproject.mc.nametag;

import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import lombok.Getter;
import io.fairyproject.container.Autowired;
import net.kyori.adventure.text.Component;

@Getter
public abstract class NameTagAdapter {

    @Autowired
    protected static NameTagService NAMETAG_SERVICE;

    public static NameTag createNametag(Component prefix, Component suffix) {
        return NAMETAG_SERVICE.getOrCreate(prefix, suffix);
    }

    public static NameTag createNametag(String legacyPrefix, String legacySuffix) {
        return NAMETAG_SERVICE.getOrCreate(MCAdventure.LEGACY.deserialize(legacyPrefix), MCAdventure.LEGACY.deserialize(legacySuffix));
    }

    private final String name;
    private final int weight;

    public NameTagAdapter(final String name, final int weight) {
        this.name = name;
        this.weight = weight;
    }

    public abstract NameTag fetch(final MCPlayer player, final MCPlayer target);

}
