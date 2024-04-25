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

package io.fairyproject.bukkit.mc.registry;

import io.fairyproject.bukkit.mc.BukkitMCWorld;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.registry.MCWorldRegistry;
import io.fairyproject.metadata.MetadataKey;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BukkitMCWorldRegistry implements MCWorldRegistry {

    private final MetadataKey<MCWorld> KEY = MetadataKey.create("fairy:mc-world", MCWorld.class);
    private final BukkitAudiences bukkitAudiences;

    @Override
    public MCWorld convert(Object worldObj) {
        if (!(worldObj instanceof World)) {
            throw new UnsupportedOperationException();
        }
        World world = (World) worldObj;
        return Metadata.provideForWorld(world).getOrPut(KEY, () -> new BukkitMCWorld(world, bukkitAudiences));
    }

    @Override
    public MCWorld getByName(String name) {
        final World world = Bukkit.getWorld(name);
        if (world == null) {
            return null;
        }
        return this.convert(world);
    }

    @Override
    public List<MCWorld> all() {
        return Bukkit.getWorlds().stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
