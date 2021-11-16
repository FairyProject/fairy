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

package io.fairyproject.bukkit.chunk;

import io.fairyproject.Fairy;
import io.fairyproject.bean.PostDestroy;
import io.fairyproject.bean.PostInitialize;
import io.fairyproject.bean.Service;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.EventSubscription;
import io.fairyproject.bukkit.listener.events.Events;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashSet;
import java.util.Set;

@Service(name = "keepChunk")
public class KeepChunkService {

    private Set<Long> chunksToKeep;
    private EventSubscription<ChunkUnloadEvent> eventSubscription;

    @PostInitialize
    public void init() {
        this.chunksToKeep = new HashSet<>();
        this.eventSubscription = Events.subscribe(ChunkUnloadEvent.class)
                .listen((sub, event) -> {
                    Chunk chunk = event.getChunk();
                    if (Fairy.isRunning() && isChunkToKeep(chunk.getX(), chunk.getZ())) {
                        event.setCancelled(true);
                    }
                }).build(FairyBukkitPlatform.PLUGIN);
    }

    @PostDestroy
    public void stop() {
        this.eventSubscription.unregister();
    }

    public void addChunk(int x, int z) {
        this.chunksToKeep.add(LongHash.toLong(x, z));
    }

    public void removeChunk(int x, int z) {
        this.chunksToKeep.remove(LongHash.toLong(x, z));
    }

    public boolean isChunkToKeep(int x, int z) {
        return this.chunksToKeep.contains(LongHash.toLong(x, z));
    }

}
