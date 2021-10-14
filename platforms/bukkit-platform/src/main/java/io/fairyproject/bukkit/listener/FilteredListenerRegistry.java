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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.fairyproject.bukkit.listener.annotation.IgnoredFilters;
import io.fairyproject.bukkit.listener.annotation.PlayerSearchAttribute;
import io.fairyproject.bukkit.listener.asm.ASMEventExecutorGenerator;
import io.fairyproject.bukkit.listener.asm.ClassDefiner;
import io.fairyproject.bukkit.listener.asm.MethodHandleEventExecutor;
import io.fairyproject.bukkit.listener.asm.StaticMethodHandleEventExecutor;
import io.fairyproject.bukkit.listener.timings.TimedEventExecutor;
import io.fairyproject.bukkit.player.PlayerEventRecognizer;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

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

    private final ConcurrentMap<Method, Class<? extends EventExecutor>> eventExecutorMap = new ConcurrentHashMap<Method, Class<? extends EventExecutor>>() {
        @NonNull
        @Override
        public Class<? extends EventExecutor> computeIfAbsent(@NonNull Method key, @NonNull Function<? super Method, ? extends Class<? extends EventExecutor>> mappingFunction) {
            Class<? extends EventExecutor> executorClass = get(key);
            if (executorClass != null)
                return executorClass;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (key) {
                executorClass = get(key);
                if (executorClass != null)
                    return executorClass;

                return super.computeIfAbsent(key, mappingFunction);
            }
        }
    };

    @NonNull
    private EventExecutor create(@NonNull Method m, @NonNull Class<? extends Event> eventClass, boolean ignoredFilters, FilteredEventList eventList) {
        Preconditions.checkNotNull(m, "Null method");
        Preconditions.checkArgument(m.getParameterCount() != 0, "Incorrect number of arguments %s", m.getParameterCount());
        Preconditions.checkArgument(m.getParameterTypes()[0] == eventClass, "First parameter %s doesn't match event class %s", m.getParameterTypes()[0], eventClass);
        ClassDefiner definer = ClassDefiner.getInstance();
        if (Modifier.isStatic(m.getModifiers())) {
            return new StaticMethodHandleEventExecutor(eventClass, m, ignoredFilters, eventList);
        } else if (definer.isBypassAccessChecks() || Modifier.isPublic(m.getDeclaringClass().getModifiers()) && Modifier.isPublic(m.getModifiers())) {
            // get the existing generated EventExecutor class for the Method or generate one
            Class<? extends EventExecutor> executorClass = eventExecutorMap.computeIfAbsent(m, (__) -> {
                String name = ASMEventExecutorGenerator.generateName();
                byte[] classData = ASMEventExecutorGenerator.generateEventExecutor(m, name);
                return definer.defineClass(m.getDeclaringClass().getClassLoader(), name, classData).asSubclass(EventExecutor.class);
            });

            Class<? extends PlayerEventRecognizer.Attribute<?>>[] attributes;
            final PlayerSearchAttribute annotation = m.getAnnotation(PlayerSearchAttribute.class);
            if (annotation != null) {
                attributes = annotation.value();
            } else {
                attributes = new Class[0];
            }

            try {
                EventExecutor asmExecutor = executorClass.newInstance();
                // Define a wrapper to conform to bukkit stupidity (passing in events that don't match and wrapper exception)
                return (listener, event) -> {
                    if (eventClass.isInstance(event) && (ignoredFilters || eventList.check(event, attributes))) {
                        asmExecutor.execute(listener, event);
                    }
                };
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AssertionError("Unable to initialize generated event executor", e);
            }
        } else {
            return new MethodHandleEventExecutor(eventClass, m, ignoredFilters, eventList);
        }
    }

    private static HandlerList getHandlerList(Class<? extends Event> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("getHandlerList");
            return (HandlerList) method.invoke(null);
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getHandlerList(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("An error occurs while invoking for getHandlerList()", e);
        }
    }

}
