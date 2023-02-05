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

package io.fairyproject.bukkit.configuration;

import io.fairyproject.bukkit.mc.BukkitMCServer;
import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.mc.entity.BukkitEntityIDCounter;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperatorImpl;
import io.fairyproject.bukkit.mc.registry.BukkitMCEntityRegistry;
import io.fairyproject.bukkit.mc.registry.BukkitMCGameProfileRegistry;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerPlatformOperator;
import io.fairyproject.bukkit.mc.registry.BukkitMCWorldRegistry;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.nms.BukkitNMSManagerImpl;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.configuration.Configuration;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.entity.EntityIDCounter;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.registry.MCEntityRegistry;
import io.fairyproject.mc.registry.MCGameProfileRegistry;
import io.fairyproject.mc.registry.player.MCPlayerPlatformOperator;
import io.fairyproject.mc.registry.MCWorldRegistry;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import lombok.Getter;
import net.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;

@Configuration
@Getter
public class BukkitMCConfiguration {

    @InjectableComponent
    public Server provideBukkitServer() {
        return Bukkit.getServer();
    }

    @InjectableComponent
    public MCServer provideMCServer(Server server) {
        return new BukkitMCServer(server);
    }

    @InjectableComponent
    public BukkitNMSManager provideNMSManager(MCServer mcServer, MCVersionMappingRegistry registry, Server server) {
        return new BukkitNMSManagerImpl(mcServer, registry, server.getClass());
    }

    @InjectableComponent
    public BukkitMCPlayerOperator providePlayerOperator(BukkitNMSManager nmsManager) {
        return new BukkitMCPlayerOperatorImpl(nmsManager);
    }

    @InjectableComponent
    public BukkitDataWatcherConverter provideDataWatcherConverter(BukkitNMSManager nmsManager) {
        return new BukkitDataWatcherConverter(nmsManager);
    }

    @InjectableComponent
    public EntityIDCounter provideEntityIDCounter(BukkitNMSManager nmsManager) {
        return new BukkitEntityIDCounter(nmsManager);
    }

    @InjectableComponent
    public MCAdventure.AdventureHook provideAdventureHook() {
        return MCAdventure.AdventureHook.builder()
                .serializer(NBTLegacyHoverEventSerializer.get())
                .build();
    }

    @InjectableComponent
    public MCEntityRegistry provideEntityRegistry(BukkitDataWatcherConverter dataWatcherConverter) {
        return new BukkitMCEntityRegistry(dataWatcherConverter);
    }

    @InjectableComponent
    public MCWorldRegistry provideWorldRegistry() {
        return new BukkitMCWorldRegistry();
    }

    @InjectableComponent
    public MCPlayerPlatformOperator provideMCPlayerPlatformOperator(
            MCServer mcServer,
            MCProtocol mcProtocol,
            BukkitDataWatcherConverter dataWatcherConverter,
            BukkitMCPlayerOperator playerOperator,
            MCVersionMappingRegistry versionMappingRegistry) {
        return new BukkitMCPlayerPlatformOperator(mcServer, mcProtocol, dataWatcherConverter, playerOperator, versionMappingRegistry);
    }

    @InjectableComponent
    public MCGameProfileRegistry provideGameProfileBridge() {
        return new BukkitMCGameProfileRegistry();
    }
}
