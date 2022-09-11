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

package io.fairyproject.bukkit.impl.server;

import io.fairyproject.Fairy;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.bukkit.impl.test.ImplementationFactory;
import io.fairyproject.bukkit.player.movement.MovementListener;
import io.fairyproject.bukkit.player.movement.impl.AbstractMovementImplementation;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import io.fairyproject.bukkit.impl.annotation.ProviderTestImpl;
import io.fairyproject.bukkit.impl.annotation.ServerImpl;
import io.fairyproject.mc.util.BlockPosition;

import java.util.*;

public interface ServerImplementation {

    @SneakyThrows
    static ServerImplementation load(ContainerContext containerContext) {
        ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .overrideClassLoaders(ServerImplementation.class.getClassLoader())
                .acceptPackages(Fairy.getFairyPackage())
                .scan();

        Class<?> lastSuccess = null;
        int priority = Integer.MIN_VALUE;
        lookup: for (Class<?> type : scanResult.getClassesWithAnnotation(ServerImpl.class).loadClasses()) {
            if (!ServerImplementation.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("The type " + type.getName() + " does not implement to ProtocolCheck!");
            }

            final ServerImpl annotation = type.getAnnotation(ServerImpl.class);
            ImplementationFactory.TestResult result = ImplementationFactory.test(type.getAnnotation(ProviderTestImpl.class));
            switch (result) {
                case NO_PROVIDER:
                    if (annotation.value() > priority) {
                        lastSuccess = type;
                        priority = annotation.value();
                    }
                    break;
                case SUCCESS:
                    if (annotation.value() > priority) {
                        lastSuccess = type;
                    }
                    break lookup;
                case FAILURE:
                    break;
            }
        }

        if (lastSuccess == null) {
            throw new UnsupportedOperationException("Couldn't find any usable protocol check! (but it's shouldn't be possible)");
        }

        return (ServerImplementation) lastSuccess.newInstance();
    }

    Entity getEntity(UUID uuid);

    Entity getEntity(World world, int id);

    default Entity getEntity(int id) {
        for (World world : Bukkit.getWorlds()) {
            Entity entity = this.getEntity(world, id);
            if (entity != null) {
                return entity;
            }
        }

        return null;
    }

    void showDyingNPC(Player player);

    @Deprecated
    Object toBlockNMS(MaterialData materialData);

    List<Player> getPlayerRadius(Location location, double radius);

    @Deprecated
    float getBlockSlipperiness(Material material);

    void sendEntityTeleport(Player player, Location location, int id);

    void sendEntityAttach(Player player, int type, int toAttach, int attachTo);

    void setSkullGameProfile(ItemMeta itemMeta, Player player);

    boolean isServerThread();

    boolean callMoveEvent(Player player, Location from, Location to);

    AbstractMovementImplementation movement(MovementListener movementListener);
}
