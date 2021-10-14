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

package io.fairyproject.bukkit.reflection;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import io.fairyproject.bean.PreInitialize;
import io.fairyproject.bean.Service;

@Service(name = "protocollib")
public class ProtocolLibService {

    private boolean enabled;

    @PreInitialize
    public void onInitialize() {
        this.enabled = true;
    }

    public void validEnabled() {
        Preconditions.checkArgument(this.enabled, "ProtocolLib isn't enabled! this feature couldn't work!");
    }

    public void send(Player player, Object packetContainer) {
        Preconditions.checkArgument(packetContainer instanceof PacketContainer, "ProtocolLibService.send(Player, Object) must be PacketContainer in second parameter!");
        try {
            this.manager().sendServerPacket(player, (PacketContainer) packetContainer);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Error while sending packet", throwable);
        }
    }

    public ProtocolManager manager() {
        return ProtocolLibrary.getProtocolManager();
    }

}
