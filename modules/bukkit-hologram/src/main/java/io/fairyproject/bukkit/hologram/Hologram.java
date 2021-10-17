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

package io.fairyproject.bukkit.hologram;

import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.hologram.api.TextViewHandler;
import io.fairyproject.bukkit.hologram.api.ViewHandler;
import io.fairyproject.bukkit.hologram.player.PlayerViewHolograms;
import io.fairyproject.bukkit.packet.wrapper.other.Vector3D;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Hologram {

    private static final float Y_PER_LINE = 0.25F;
    private static int NEW_ID = 0;

    private HologramHandler hologramHandler;

    private int id;
    private Location location;
    private boolean spawned;

    private Entity attachedTo;
    private InteractListener interactListener;

    private List<HologramSingle> lines = new ArrayList<>();
    private List<Player> renderedPlayers = Collections.synchronizedList(new ArrayList<>());

    public Hologram(Location location, HologramHandler hologramHandler) {
        this.id = NEW_ID++;
        this.location = location;
        this.hologramHandler = hologramHandler;
    }

    public double getX() {
        return this.location.getX();
    }

    public double getY() {
        return this.location.getY();
    }

    public double getZ() {
        return this.location.getZ();
    }

    public World getWorld() {
        return this.location.getWorld();
    }

    public void addText(String text) {
        this.addView(new TextViewHandler(text));
    }

    public void addView(ViewHandler viewHandler) {
        this.setView(this.lines.size(), viewHandler);
    }

    public void setText(int index, String text) {
        this.setView(index, new TextViewHandler(text));
    }

    public void update() {
        this.validateMainThread();
        this.lines.forEach(hologram -> hologram.sendNamePackets(this.renderedPlayers));
    }

    public void setView(int index, ViewHandler viewHandler) {
        this.validateMainThread();
        if (index >= this.lines.size()) {
            HologramSingle single = new HologramSingle(this, viewHandler, -Y_PER_LINE * index, index);
            this.lines.add(index, single);
            this.hologramHandler.registerEntityId(single.getHorseId(), this);
            this.hologramHandler.registerEntityId(single.getArmorStandId(), this);

            if (this.isSpawned()) {
                single.send(this.renderedPlayers);
            }
        } else {
            HologramSingle single = this.lines.get(index);
            if (single.getIndex() != index) {
                this.lines.add(index, single);
            }

            single.setViewHandler(viewHandler);
            single.sendNamePackets(this.renderedPlayers);
        }

    }

    public void removeView(int index) {
        this.validateMainThread();
        if (lines.size() > index) {
            HologramSingle single = this.lines.get(index);
            single.sendRemove(this.renderedPlayers);
            this.lines.remove(index);

            this.hologramHandler.unregisterEntityId(single.getHorseId());
            this.hologramHandler.unregisterEntityId(single.getArmorStandId());
        }
    }

    public void setLocation(Location location) {
        this.move(location);
    }

    private void move(@NonNull Location location) {
        this.validateMainThread();
        if (this.location.equals(location)) {
            return;
        }

        if (!this.location.getWorld().equals(location.getWorld())) {
            throw new IllegalArgumentException("cannot move to different world");
        }

        this.location = location;

        if (this.isSpawned()) {

            List<Player> players = this.location.getWorld().getPlayers();
            this.lines.forEach(hologram -> hologram.sendTeleportPacket(players));

        }
    }

    public boolean isAttached() {
        return attachedTo != null;
    }

    public void spawnPlayer(Player player) {
        this.validateMainThread();
        this.lines.forEach(hologram -> hologram.send(Collections.singleton(player)));
        this.renderedPlayers.add(player);
    }

    protected List<Player> getNearbyPlayers() {
        this.validateMainThread();
        return Imanity.IMPLEMENTATION.getPlayerRadius(this.location, HologramHandler.DISTANCE_TO_RENDER);
    }

    public void spawn() {
        this.validateMainThread();
        this.validateDespawned();

        this.getNearbyPlayers()
                .forEach(this.hologramHandler::update);

        this.spawned = true;
    }

    public void removePlayer(Player player) {
        this.validateMainThread();
        this.lines.forEach(hologram -> hologram.sendRemove(Collections.singleton(player)));
        this.renderedPlayers.remove(player);
    }

    public boolean remove() {
        this.validateMainThread();
        this.validateSpawned();

        for (Player player : new ArrayList<>(this.renderedPlayers)) {
            PlayerViewHolograms holograms = this.hologramHandler.getRenderedHolograms(player);
            holograms.removeHologram(player, this);
        }
        this.renderedPlayers.clear();
        this.hologramHandler.removeHologram(this);

        for (HologramSingle line : this.lines) {
            this.hologramHandler.unregisterEntityId(line.getHorseId());
            this.hologramHandler.unregisterEntityId(line.getArmorStandId());
        }

        this.spawned = false;
        return true;
    }

    public double distaneTo(Player player) {
        return Math.sqrt(Math.pow(this.getLocation().getX() - player.getLocation().getX(), 2)
                + Math.pow(this.getLocation().getZ() - player.getLocation().getZ(), 2));
    }

    private void validateSpawned() {
        if (!this.spawned)
            throw new IllegalStateException("Not spawned");
    }

    private void validateDespawned() {
        if (this.spawned)
            throw new IllegalStateException("Already spawned");
    }

    private void validateMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Hologram doesn't support async");
        }
    }

    public interface InteractListener {

        default void attack(Player player) {

        }

        default void interact(Player player) {

        }

        default void interactAt(Player player, Vector3D vector3D) {

        }

    }

}
