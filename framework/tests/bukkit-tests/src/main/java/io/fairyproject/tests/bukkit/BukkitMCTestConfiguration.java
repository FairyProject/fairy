package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.configuration.TestConfiguration;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.registry.player.MCPlayerPlatformOperator;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.tests.bukkit.mc.operator.BukkitMCPlayerOperatorMock;
import io.fairyproject.tests.bukkit.mc.registry.BukkitMCPlayerPlatformOperatorMock;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

@TestConfiguration
public class BukkitMCTestConfiguration {

    @InjectableComponent
    public BukkitNMSManager provideNMSManager() {
        return new BukkitNMSManagerMock();
    }

    @InjectableComponent
    public BukkitMCPlayerOperator providePlayerOperator() {
        return new BukkitMCPlayerOperatorMock();
    }

    @InjectableComponent
    public MCPlayerPlatformOperator provideMCPlayerPlatformOperator(
            MCServer mcServer,
            BukkitAudiences bukkitAudiences,
            BukkitDataWatcherConverter dataWatcherConverter,
            BukkitMCPlayerOperator playerOperator,
            MCSchedulerProvider mcSchedulerProvider,
            MCVersionMappingRegistry versionMappingRegistry) {
        return new BukkitMCPlayerPlatformOperatorMock(mcServer, bukkitAudiences, dataWatcherConverter, playerOperator, mcSchedulerProvider, versionMappingRegistry);
    }
}
