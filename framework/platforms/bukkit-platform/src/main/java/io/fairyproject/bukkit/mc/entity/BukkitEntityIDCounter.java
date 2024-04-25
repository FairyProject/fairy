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

package io.fairyproject.bukkit.mc.entity;

import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.mc.entity.EntityIDCounter;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.github.toolfactory.narcissus.Narcissus;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitEntityIDCounter implements EntityIDCounter {

    private final BukkitNMSManager nmsManager;
    private Function<Integer, Integer> next;

    public BukkitEntityIDCounter(BukkitNMSManager nmsManager) {
        this.nmsManager = nmsManager;
    }

    @Override
    public int next() {
        if (this.next == null) {
            Class<?> entityClass = nmsManager.getNmsClassResolver().resolveSilent("world.entity.Entity", "Entity");

            Function<Integer, Integer> function;
            try {
                FieldWrapper fieldWrapper = new FieldResolver(entityClass).resolveWrapper("entityCount");
                function = n -> {
                    int id = (int) fieldWrapper.get(null);
                    fieldWrapper.set(null, id + n);
                    return id; // it's id++ so we return the old one
                };
            } catch (Throwable throwable) {
                AtomicInteger entityCounter = null;

                try {
                    Field field = new FieldResolver(entityClass).resolve(new ResolverQuery(AtomicInteger.class, 0)
                            .withModifierOptions(ResolverQuery.ModifierOptions.builder()
                                    .onlyStatic(true)
                                    .onlyFinal(true)
                                    .build())
                    );

                    try {
                        entityCounter = (AtomicInteger) field.get(null);
                    } catch (IllegalAccessException | InternalError ex) {
                        if (Narcissus.libraryLoaded) {
                            entityCounter = (AtomicInteger) Narcissus.getStaticField(field);
                        } else {
                            throw new IllegalStateException("Couldn't get the entity counter field!", ex);
                        }
                    }
                } catch (ReflectiveOperationException e) {
                    SneakyThrowUtil.sneakyThrow(e);
                }

                function = entityCounter::addAndGet;
            }

            this.next = function;
        }
        return this.next.apply(1);
    }
}
