/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bukkit.protocol;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.fairyproject.FairyPlatform;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.mc.protocol.PacketEventsBuilder;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class BukkitPacketEventsBuilder implements PacketEventsBuilder {

    /**
     * Controls PacketEvents' {@code reEncodeByDefault} setting, toggleable at startup via the
     * system property {@code -Dfairy.packetevents.re-encode=true|false}.
     * <p>
     * Defaults to {@code false}: packets are passed through untouched unless a listener modifies them.
     * Set it to {@code true} when relying on BungeeCord/Velocity legacy IP-forwarding on 1.8.x backends,
     * where the handshake must be re-encoded for the forwarded {@code host\0ip\0uuid\0textures} payload
     * to reach the backend (otherwise the player logs in with a random offline UUID and no skin).
     */
    private static final boolean RE_ENCODE_BY_DEFAULT = Boolean.getBoolean("fairy.packetevents.re-encode");

    public final FairyPlatform platform;

    @Override
    public PacketEventsAPI<?> build() {
        PacketEventsAPI<Plugin> packetEventsAPI = SpigotPacketEventsBuilder.buildNoCache(FairyBukkitPlatform.PLUGIN);
        PacketEventsSettings settings = packetEventsAPI.getSettings();
        settings.reEncodeByDefault(RE_ENCODE_BY_DEFAULT);

        return packetEventsAPI;
    }

}
