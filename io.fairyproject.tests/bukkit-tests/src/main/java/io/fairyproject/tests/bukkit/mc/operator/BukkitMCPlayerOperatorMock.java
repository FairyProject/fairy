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

package io.fairyproject.tests.bukkit.mc.operator;

import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class BukkitMCPlayerOperatorMock implements BukkitMCPlayerOperator {
    @Override
    public int getPing(Player player) {
        return 0;
    }

    @Override
    public MCGameProfile getGameProfile(Player player) {
        return null;
    }

    @Override
    public Component getDisplayName(Player player) {
        return null;
    }

    @Override
    public void setDisplayName(MCPlayer player, Component displayName) {

    }

    @Override
    public Channel getChannel(Player player) {
        return null;
    }
}
