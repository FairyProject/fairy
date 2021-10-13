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

package org.fairy.bukkit.bossbar;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.fairy.bukkit.packet.PacketService;
import org.fairy.bukkit.packet.wrapper.server.WrappedPacketOutSpawnEntityLiving;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.bukkit.reflection.minecraft.DataWatcher;
import org.fairy.bukkit.reflection.resolver.ConstructorResolver;
import org.fairy.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.fairy.bukkit.reflection.wrapper.DataWatcherWrapper;
import org.fairy.bukkit.reflection.wrapper.PacketWrapper;
import org.fairy.mc.protocol.MCVersion;
import org.fairy.util.Utility;

import static org.fairy.bukkit.reflection.minecraft.DataWatcher.V1_9.ValueType.*;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class BossBar {

    private static final int ENTITY_DISTANCE = 32;
    private static final int WITHER_MAX_HEALTH = 300;
    private static final int DRAGON_MAX_HEALTH = 200;

    private final int entityId;

    private final Player player;
    private final MCVersion version;

    private boolean visible;
    private String previousText;
    private float previousHealth;
    private DataWatcherWrapper dataWatcher;

    @Setter
    private long lastUpdate;

    @Getter
    private final AtomicBoolean moved = new AtomicBoolean(false);

    public BossBar(Player player) {
        this.player = player;

        this.version = MinecraftReflection.getProtocol(player);
        this.entityId = MinecraftReflection.getNewEntityId();

        this.buildPackets();
    }

    public float getMaxHealth() {
        return this.version == MCVersion.v1_7 ? DRAGON_MAX_HEALTH : WITHER_MAX_HEALTH;
    }

    public Location makeLocation(Location base) {

        switch (this.version) {
            case v1_7:
                Location location = base.clone();
                location.setY(-200);
                return location;
            default:
                return base.getDirection().multiply(ENTITY_DISTANCE).add(base.toVector()).toLocation(base.getWorld());
        }
    }

    private WrappedPacketOutSpawnEntityLiving packetWither;

    private void buildPackets() {
        this.packetWither = new WrappedPacketOutSpawnEntityLiving(
                this.entityId,
                64,
                0,
                0,
                0,
                0.0F,
                0.0F,
                0.0F,
                0.0D,
                0.0D,
                0.0D,
                dataWatcher,
                null
        );
    }

    private void buildDataWatcher() {
        this.dataWatcher = DataWatcherWrapper.create(null);

        if (this.version != MCVersion.v1_7) {
            this.dataWatcher.setValue(17, ENTITY_WITHER_a, 0);
            this.dataWatcher.setValue(18, ENTITY_WIHER_b, 0);
            this.dataWatcher.setValue(19, ENTITY_WITHER_c, 0);

            this.dataWatcher.setValue(20, ENTITY_WITHER_bw, 880);
        }
        this.dataWatcher.setValue(0, ENTITY_FLAG, (byte) (1 << 5));
    }

    private void updateDataWatcher(BossBarData bossBarData) {
        if (this.dataWatcher == null) {
            this.buildDataWatcher();
        }

        this.dataWatcher.setValue(6, ENTITY_LIVING_HEALTH, bossBarData.getHealth() / 100.0F * this.getMaxHealth());
        this.dataWatcher.setValue(2, ENTITY_NAME, bossBarData.getText());
        this.dataWatcher.setValue(3, ENTITY_NAME_VISIBLE, (byte) 1);
    }

    private void sendMetadata(BossBarData bossBarData) {
        if (bossBarData.getText().equals(this.previousText)
            && bossBarData.getHealth() == this.previousHealth) {
            return;
        }

        Object dataWatcher = this.dataWatcher.getDataWatcherObject();

        this.previousText = bossBarData.getText();
        this.previousHealth = bossBarData.getHealth();

        Utility.tryCatch(() -> {
            NMSClassResolver classResolver = new NMSClassResolver();
            ConstructorResolver constructorResolver = new ConstructorResolver(classResolver.resolve("PacketPlayOutEntityMetadata"));
            Object packetMetadata = constructorResolver
                    .resolveWrapper(new Class[] {int.class, DataWatcher.TYPE, boolean.class})
                    .newInstanceSilent(this.entityId, dataWatcher, true);
            MinecraftReflection.sendPacket(player, packetMetadata);
        });
    }

    private void sendMovement() {
        Location location = this.makeLocation(player.getLocation());

        PacketWrapper packetTeleport = PacketWrapper.createByPacketName("PacketPlayOutEntityTeleport");
        packetTeleport.setPacketValue("a", this.entityId);

        packetTeleport.setPacketValue("b", (int) (location.getX() * 32D));
        packetTeleport.setPacketValue("c", (int) (location.getY() * 32D));
        packetTeleport.setPacketValue("d", (int) (location.getZ() * 32D));

        packetTeleport.setPacketValue("e", (byte) (int) (location.getYaw() * 256F / 360F));
        packetTeleport.setPacketValue("f", (byte) (int) (location.getPitch() * 256F / 360F));

        MinecraftReflection.sendPacket(player, packetTeleport);
    }

    public void send(BossBarData bossBarData) {

        boolean movement = this.moved.get();

        if (!this.visible) {
            this.visible = true;
            this.updateDataWatcher(bossBarData);
            this.buildPackets();

            PacketService.send(player, this.packetWither);
            movement = true;
        } else {
            this.updateDataWatcher(bossBarData);
        }

        this.sendMetadata(bossBarData);
        if (movement) {
            this.sendMovement();
        }

    }

    public void destroy(Player player) {
        if (!visible) {
            return;
        }

        PacketWrapper packetDestroy = PacketWrapper.createByPacketName("PacketPlayOutEntityDestroy");
        packetDestroy.setPacketValue("a", new int[] {this.entityId});
        MinecraftReflection.sendPacket(player, packetDestroy);
        this.visible = false;
    }

}
