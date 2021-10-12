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

package org.fairy.bean;

import org.fairy.Fairy;
import org.fairy.reflect.ReflectLookup;
import org.fairy.util.entry.Entry;
import org.fairy.util.entry.EntryArrayList;
import org.fairy.bean.details.*;

import java.util.ArrayList;
import java.util.List;

public class ComponentRegistry {

    private static final EntryArrayList<Class<?>, ComponentHolder> COMPONENT_HOLDERS = new EntryArrayList<>();

    public static void registerComponentHolder(ComponentHolder componentHolder) {
        for (Class<?> type : componentHolder.type()) {
            COMPONENT_HOLDERS.add(type, componentHolder);
        }
    }

    public static ComponentHolder getComponentHolder(Class<?> type) {

        for (Entry<Class<?>, ComponentHolder> entry : COMPONENT_HOLDERS) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }

        return null;

    }

    static void registerComponentHolders() {
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {Thread.class};
            }

            @Override
            public void onEnable(Object instance) {
                Thread thread = (Thread) instance;

                Fairy.getTaskScheduler().runSync(thread::start); // Don't start Immediately
            }
        });
    }

    public static List<ComponentBeanDetails> scanComponents(BeanContext beanContext, ReflectLookup reflectLookup) {
        List<ComponentBeanDetails> components = new ArrayList<>();

        for (Class<?> type : reflectLookup.findAnnotatedClasses(Component.class)) {
            try {
                Component component = type.getAnnotation(Component.class);

                ComponentHolder componentHolder = ComponentRegistry.getComponentHolder(type);
                if (componentHolder == null) {
                    if (component.throwIfNotRegistered()) {
                        BeanContext.LOGGER.error("No ComponentHolder was registered for class " + type.getName() + "!");
                    }
                    continue;
                }

                Object instance = componentHolder.newInstance(type);

                if (instance != null) {
                    final ComponentBeanDetails beanDetails = beanContext.registerComponent(instance, type, componentHolder);
                    if (beanDetails != null) {
                        components.add(beanDetails);
                    }
                }
            } catch (Throwable throwable) {
                BeanContext.LOGGER.error("Something wrong will scanning component for " + type.getName(), throwable);
            }
        }

        return components;
    }

}
