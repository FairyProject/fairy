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

package io.fairyproject.mc;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.mc.entity.EntityIDCounter;
import io.fairyproject.mc.registry.MCEntityRegistry;
import io.fairyproject.mc.registry.MCGameProfileRegistry;
import io.fairyproject.mc.registry.MCPlayerRegistry;
import io.fairyproject.mc.registry.MCWorldRegistry;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@InjectableComponent
@RequiredArgsConstructor
public class MCLegacyInitializer {

    private final MCServer mcServer;
    private final MCEntityRegistry mcEntityRegistry;
    private final MCGameProfileRegistry gameProfileRegistry;
    private final MCPlayerRegistry playerRegistry;
    private final MCWorldRegistry worldRegistry;
    private final EntityIDCounter entityIDCounter;

    @PreInitialize
    public void onPreInitialize() {
        MCServer.Companion.CURRENT = mcServer;
        MCEntity.Companion.BRIDGE = mcEntityRegistry::convert;
        MCGameProfile.Companion.BRIDGE = new MCGameProfile.Bridge() {
            @Override
            public MCGameProfile create(String name, UUID uuid) {
                return gameProfileRegistry.create(name, uuid);
            }

            @Override
            public MCGameProfile from(Object object) {
                return gameProfileRegistry.convert(object);
            }
        };
        MCPlayer.Companion.BRIDGE = new MCPlayer.Bridge() {
            @Override
            public UUID from(@NotNull Object obj) {
                return playerRegistry.from(obj);
            }

            @Override
            public MCPlayer find(UUID uuid) {
                return playerRegistry.find(uuid);
            }

            @Override
            public MCPlayer create(Object obj) {
                return playerRegistry.create(obj);
            }

            @Override
            public Collection<MCPlayer> all() {
                return playerRegistry.all();
            }
        };
        MCWorld.Companion.BRIDGE = new MCWorld.Bridge() {
            @Override
            public MCWorld from(Object world) {
                return worldRegistry.convert(world);
            }

            @Override
            public MCWorld getByName(String name) {
                return worldRegistry.getByName(name);
            }

            @Override
            public List<MCWorld> all() {
                return worldRegistry.all();
            }
        };
        EntityIDCounter.Companion.CURRENT = entityIDCounter;
    }

}
