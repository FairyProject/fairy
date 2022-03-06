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

package io.fairyproject.mc.hologram;

import com.github.retrooper.packetevents.util.Vector3f;
import io.fairyproject.mc.hologram.api.TextViewHandler;
import io.fairyproject.mc.hologram.api.ViewHandler;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.util.Pos;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Hologram {

    private static final float Y_PER_LINE = 0.25F;
    private static int NEW_ID = 0;

    private HologramFactory hologramFactory;

    private int id;
    private Pos position;
    private boolean spawned;

    private MCEntity attachedTo;
    private InteractListener interactListener;

    private List<SingleHologram> lines = new ArrayList<>();
    private List<MCPlayer> renderedPlayers = Collections.synchronizedList(new ArrayList<>());

    public Hologram(Pos position, HologramFactory hologramFactory) {
        this.id = NEW_ID++;
        this.position = position;
        this.hologramFactory = hologramFactory;
    }

    public double getX() {
        return this.position.getX();
    }

    public double getY() {
        return this.position.getY();
    }

    public double getZ() {
        return this.position.getZ();
    }

    public MCWorld getWorld() {
        return this.position.getMCWorld();
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
            SingleHologram single = new SingleHologram(this, viewHandler, -Y_PER_LINE * index, index);
            this.lines.add(index, single);
            this.hologramFactory.registerEntityId(single.getHorseId(), this);
            this.hologramFactory.registerEntityId(single.getArmorStandId(), this);

            if (this.isSpawned()) {
                single.send(this.renderedPlayers);
            }
        } else {
            SingleHologram single = this.lines.get(index);
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
            SingleHologram single = this.lines.get(index);
            single.sendRemove(this.renderedPlayers);
            this.lines.remove(index);

            this.hologramFactory.unregisterEntityId(single.getHorseId());
            this.hologramFactory.unregisterEntityId(single.getArmorStandId());
        }
    }

    public void setPosition(Pos position) {
        this.move(position);
    }

    private void move(@NonNull Pos location) {
        this.validateMainThread();
        if (this.position.equals(location)) {
            return;
        }

        if (!this.position.getWorld().equals(location.getWorld())) {
            throw new IllegalArgumentException("cannot move to different world");
        }

        this.position = location;
        if (this.isSpawned()) {
            List<MCPlayer> players = this.position.getMCWorld().players();
            this.lines.forEach(hologram -> hologram.sendTeleportPacket(players));
        }
    }

    public boolean isAttached() {
        return attachedTo != null;
    }

    public void spawnPlayer(MCPlayer player) {
        this.validateMainThread();
        this.lines.forEach(hologram -> hologram.send(Collections.singleton(player)));
        this.renderedPlayers.add(player);
    }

    protected List<MCPlayer> getNearbyPlayers() {
        this.validateMainThread();
        return this.position.getMCWorld().players()
                .stream()
                .filter(player -> player.pos().distanceTo(this.position) < HologramFactory.DISTANCE_TO_RENDER)
                .collect(Collectors.toList());
    }

    public void spawn() {
        this.validateMainThread();
        this.validateDespawned();

        this.getNearbyPlayers()
                .forEach(this.hologramFactory::update);

        this.spawned = true;
    }

    public void removePlayer(MCPlayer player) {
        this.validateMainThread();
        this.lines.forEach(hologram -> hologram.sendRemove(Collections.singleton(player)));
        this.renderedPlayers.remove(player);
    }

    public boolean remove() {
        this.validateMainThread();
        this.validateSpawned();

        for (MCPlayer player : new ArrayList<>(this.renderedPlayers)) {
            HologramRenderer holograms = this.hologramFactory.renderer(player);
            holograms.removeHologram(player, this);
        }
        this.renderedPlayers.clear();
        this.hologramFactory.remove(this);

        for (SingleHologram line : this.lines) {
            this.hologramFactory.unregisterEntityId(line.getHorseId());
            this.hologramFactory.unregisterEntityId(line.getArmorStandId());
        }

        this.spawned = false;
        return true;
    }

    public double distanceTo(MCPlayer player) {
        return Math.sqrt(Math.pow(this.getPosition().getX() - player.pos().getX(), 2)
                + Math.pow(this.getPosition().getZ() - player.pos().getZ(), 2));
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
        if (MCServer.current().isMainThread()) {
            throw new IllegalStateException("Hologram doesn't support async");
        }
    }

    public interface InteractListener {

        default void attack(MCPlayer player) {

        }

        default void interact(MCPlayer player) {

        }

        default void interactAt(MCPlayer player, Vector3f vector) {

        }

    }

}
