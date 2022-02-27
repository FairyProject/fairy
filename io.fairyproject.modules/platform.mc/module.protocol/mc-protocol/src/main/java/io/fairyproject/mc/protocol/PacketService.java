package io.fairyproject.mc.protocol;

import io.fairyproject.container.ComponentHolder;
import io.fairyproject.container.ComponentRegistry;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.mc.protocol.annotation.Listen;
import io.fairyproject.mc.protocol.listener.PacketListener;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.reflect.Reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
public final class PacketService {
    private final Map<PacketListener, List<InternalListener>> objectListenerCache = new HashMap<>();
    private final Queue<InternalListener> listeners = new PriorityQueue<>((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

    @PreInitialize
    public void preInit() {
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(PacketListener.class)
                .onEnable(obj -> {
                    final PacketListener packetListener = (PacketListener) obj;
                    final List<Method> methodList = Reflect.getDeclaredMethods(obj.getClass());
                    for (Method declaredMethod : methodList) {
                        try {
                            Reflect.setAccessible(declaredMethod);
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalStateException("Failed to initiate reflection on object", e);
                        }
                    }

                    final List<InternalListener> internalListeners = new ArrayList<>();

                    for (Method method : methodList) {
                        final Listen listen = method.getAnnotation(Listen.class);

                        if (listen == null)
                            continue;

                        if (method.getParameterCount() != 1) {
                            throw new IllegalStateException("PacketListener can only listen to a packet");
                        }

                        final Class<?> type = method.getParameters()[0].getType();
                        final InternalListener listener = new InternalListener(type, packetListener, method, listen.priority());
                        internalListeners.add(listener);
                    }

                    objectListenerCache.put(packetListener, internalListeners);
                    listeners.addAll(internalListeners);
                })
                .onDisable(obj -> {
                    final List<InternalListener> internalListeners = objectListenerCache.get((PacketListener) obj);
                    objectListenerCache.remove(obj);
                    listeners.removeAll(internalListeners);
                }).build());
    }

    public void call(final Packet packet) {
        listeners.stream()
                .filter(e -> e.getType().isAssignableFrom(packet.getClass()))
                .forEach(e -> {
                    try {
                        e.getMethod().invoke(e.getObj(), packet);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new IllegalStateException("Failed to invoke packet listener", ex);
                    }
                });
    }

    static class InternalListener {
        private final Class<?> type;
        private final PacketListener obj;
        private final Method method;
        private final int priority;

        public InternalListener(Class<?> type, PacketListener obj, Method method, int priority) {
            this.type = type;
            this.obj = obj;
            this.method = method;
            this.priority = priority;
        }

        public Class<?> getType() {
            return type;
        }

        public PacketListener getObj() {
            return obj;
        }

        public Method getMethod() {
            return method;
        }

        public int getPriority() {
            return priority;
        }
    }
}
