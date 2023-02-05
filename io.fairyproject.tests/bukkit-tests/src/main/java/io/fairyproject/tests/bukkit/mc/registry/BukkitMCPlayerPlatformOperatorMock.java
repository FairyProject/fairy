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

package io.fairyproject.tests.bukkit.mc.registry;

import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerPlatformOperator;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.tests.bukkit.BukkitMCPlayerMock;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.UUID;

public class BukkitMCPlayerPlatformOperatorMock extends BukkitMCPlayerPlatformOperator {

    public BukkitMCPlayerPlatformOperatorMock(
            MCServer mcServer,
            MCProtocol mcProtocol,
            BukkitDataWatcherConverter dataWatcherConverter,
            BukkitMCPlayerOperator playerOperator,
            MCVersionMappingRegistry versionMappingRegistry) {
        super(mcServer, mcProtocol, dataWatcherConverter, playerOperator, versionMappingRegistry);
    }

    @Override
    public MCPlayer create(@NotNull String name, @NotNull UUID uuid, @NotNull InetAddress address) {
        return new BukkitMCPlayerMock(uuid, name, MCVersion.of(8), versionMappingRegistry); // version customize?
    }
}
