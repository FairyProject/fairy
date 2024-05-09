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

import com.github.retrooper.packetevents.protocol.player.User;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.log.Log;
import io.fairyproject.mc.protocol.MCProtocol;
import io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This is a class that patches the issue where PacketEvents does not set existing native player object when it's reloaded.
 * This class will remain here until PacketEvents fixes it.
 *
 * @author LeeGod
 * @since 0.7.0
 */
@InjectableComponent
@RequiredArgsConstructor
public class BukkitPacketEventsPatcher {

    private final MCProtocol mcProtocol;

    @PostInitialize
    public void onPostInitialize() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = mcProtocol.getPacketEvents().getPlayerManager().getUser(player);
            if (user == null || user.getChannel() == null) {
                Log.error("Failed to patch PacketEvents for player " + player.getName() + " (" + player.getUniqueId() + ") because the user or channel is null.");
                return;
            }

            SpigotChannelInjector channelInjector = (SpigotChannelInjector) mcProtocol.getPacketEvents().getInjector();
            channelInjector.setPlayer(user.getChannel(), player);
        }
    }

}
