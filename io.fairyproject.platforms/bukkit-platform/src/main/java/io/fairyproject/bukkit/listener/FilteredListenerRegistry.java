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

package io.fairyproject.bukkit.listener;

import com.google.common.collect.ImmutableSet;
import io.fairyproject.bukkit.listener.annotation.IgnoredFilters;
import io.fairyproject.bukkit.listener.asm.MethodHandleEventExecutor;
import io.fairyproject.bukkit.listener.asm.StaticMethodHandleEventExecutor;
import io.fairyproject.bukkit.listener.timings.TimedEventExecutor;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.ConditionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilteredListenerRegistry {

    public static final FilteredListenerRegistry INSTANCE = new FilteredListenerRegistry();

    public void register(FilteredListener<?> filteredListener) {
        this.register(filteredListener, filteredListener.plugin, filteredListener.getEventList());
    }

    public <L extends Listener> void register(L listener, Plugin plugin, FilteredEventList eventList) {
        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<Class<? extends Event>, Set<RegisteredListener>>();
        Set<Method> methods;
        try {
            Method[] publicMethods = listener.getClass().getMethods();
            Method[] privateMethods = listener.getClass().getDeclaredMethods();
            ImmutableSet.Builder<Method> builder = ImmutableSet.builder();
            for (Method method : publicMethods) {
                builder.add(method);
            }
            for (Method method : privateMethods) {
                builder.add(method);
            }

            methods = builder.build();
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
            return;
        }

        for (Method method : methods) {
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (eventHandler == null) {
                continue;
            }
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            final Class<?> checkClass;
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " +listener.getClass());
                continue;
            }
            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.computeIfAbsent(eventClass, k -> new HashSet<>());

            boolean ignoredFilters = method.getAnnotation(IgnoredFilters.class) != null;

            EventExecutor executor = new TimedEventExecutor(this.create(method, eventClass, ignoredFilters, eventList), plugin, method, eventClass);
            eventSet.add(new RegisteredListener(listener, executor, eventHandler.priority(), plugin, eventHandler.ignoreCancelled()));
        }
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : ret.entrySet()) {
            getHandlerList(entry.getKey()).registerAll(entry.getValue());
        }
    }

    @NonNull
    private EventExecutor create(@NonNull Method m, @NonNull Class<? extends Event> eventClass, boolean ignoredFilters, FilteredEventList eventList) {
        ConditionUtils.notNull(m, "Null method");
        ConditionUtils.is(m.getParameterCount() != 0, "Incorrect number of arguments %s", m.getParameterCount());
        ConditionUtils.is(m.getParameterTypes()[0] == eventClass, "First parameter %s doesn't match event class %s", m.getParameterTypes()[0], eventClass);
        if (Modifier.isStatic(m.getModifiers())) {
            return new StaticMethodHandleEventExecutor(eventClass, m, ignoredFilters, eventList);
        } else {
            return new MethodHandleEventExecutor(eventClass, m, ignoredFilters, eventList);
        }
    }

    private static HandlerList getHandlerList(Class<? extends Event> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("getHandlerList");
            AccessUtil.setAccessible(method);
            return (HandlerList) method.invoke(null);
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getHandlerList(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("An error occurs while invoking for getHandlerList()", e);
        }
    }

}
