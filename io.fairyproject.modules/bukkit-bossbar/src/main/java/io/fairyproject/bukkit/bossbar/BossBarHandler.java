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

package io.fairyproject.bukkit.bossbar;

import io.fairyproject.Fairy;
import io.fairyproject.bukkit.player.movement.MovementListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.util.CC;

public class BossBarHandler implements Runnable {

    // TODO
//    public static void registerBossBarHandler(BossBarAdapter adapter) {
//        Imanity.BOSS_BAR_HANDLER = new BossBarHandler(adapter);
//    }

    public static final MetadataKey<BossBar> METADATA = MetadataKey.create(Fairy.METADATA_PREFIX + "BossBar", BossBar.class);

    private static final long v1_7_tick = 3L;
    private static final long v1_8_tick = 60L;

    private final BossBarAdapter adapter;

    public BossBarHandler(BossBarAdapter adapter) {
        this.adapter = adapter;

        Imanity.registerMovementListener(new MovementListener() {
            @Override
            public void handleUpdateLocation(Player player, Location from, Location to) {
                BossBar bossBar = getOrCreate(player);
                bossBar.getMoved().set(true);
            }

            @Override
            public void handleUpdateRotation(Player player, Location from, Location to) {
                BossBar bossBar = getOrCreate(player);
                bossBar.getMoved().set(true);
            }
        }).ignoreSameBlock();

        Events.subscribe(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();

                BossBar bossBar = getOrCreate(player);
                bossBar.destroy(player);

                Metadata
                        .provideForPlayer(player)
                        .remove(METADATA);
            }

            @EventHandler
            public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
                Player player = event.getPlayer();

                BossBar bossBar = getOrCreate(player);
                bossBar.destroy(player);
            }
        });
        Thread thread = new Thread(this);
        thread.setName("Imanity Boss Bar Thread");
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void run() {
        while (Fairy.isRunning()) {
            try {
                this.tick();
            } catch (Throwable throwable) {
                throw new RuntimeException("Something wrong while ticking boss bar", throwable);
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                throw new RuntimeException("Something wrong while ticking boss bar", e);
            }

        }
        Thread.interrupted();
    }

    private long getUpdateTick(BossBar bossBar) {
        switch (bossBar.getVersion()) {
            case V1_7:
                return v1_7_tick * 50L;
            default:
                return v1_8_tick * 50L;
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();

        for (Player player : Imanity.getPlayers()) {
            BossBar bossBar = this.getOrCreate(player);

            if (now - bossBar.getLastUpdate() < this.getUpdateTick(bossBar)) {
                continue;
            }
            bossBar.setLastUpdate(now);

            BossBarData bossBarData = this.adapter.tick(bossBar);

            if (bossBarData == null || bossBarData.getHealth() <= 0.0F) {
                bossBar.destroy(player);
                continue;
            }
            if (bossBarData.getText() == null) {
                bossBarData.setText("");
            }
            bossBarData.setText(CC.translate(bossBarData.getText()));
            bossBar.send(bossBarData);
        }
    }

    public BossBar getOrCreate(Player player) {
        return Metadata
                .provideForPlayer(player)
                .getOrPut(METADATA, () -> new BossBar(player));
    }
}
