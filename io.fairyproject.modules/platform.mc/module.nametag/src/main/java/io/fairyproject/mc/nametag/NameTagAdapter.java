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

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import net.kyori.adventure.text.Component;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import lombok.Getter;
import io.fairyproject.container.Autowired;

@Getter
public abstract class NameTagAdapter {

    @Autowired
    protected static NameTagService NAMETAG_SERVICE;

    @Deprecated
    public static NameTag createNametag(Component prefix, Component suffix) {
        return createNametag(prefix, suffix, NameTagVisibility.ALWAYS);
    }

    @Deprecated
    public static NameTag createNametag(String legacyPrefix, String legacySuffix) {
        return createNametag(legacyPrefix, legacySuffix, NameTagVisibility.ALWAYS);
    }

    @Deprecated
    public static NameTag createNametag(Component prefix, Component suffix, NameTagVisibility nameTagVisibility) {
        return NameTag.builder()
                .prefix(prefix)
                .suffix(suffix)
                .nameTagVisibility(nameTagVisibility)
                .build();
    }

    @Deprecated
    public static NameTag createNametag(String legacyPrefix, String legacySuffix, NameTagVisibility nameTagVisibility) {
        return NameTag.builder()
                .prefix(MCAdventure.LEGACY.deserialize(legacyPrefix))
                .suffix(MCAdventure.LEGACY.deserialize(legacySuffix))
                .nameTagVisibility(nameTagVisibility)
                .build();
    }

    private final String name;
    private final int weight;

    public NameTagAdapter(final String name, final int weight) {
        this.name = name;
        this.weight = weight;
    }

    public abstract NameTag fetch(final MCPlayer player, final MCPlayer target);

}
