package io.fairyproject.mc.protocol;

import io.fairyproject.mc.MCPlayer;

import java.util.HashSet;
import java.util.UUID;

public abstract class PacketProvider {
    protected final InternalPacketListener highListener;
    protected final InternalBufferListener lowListener;
    protected final PacketInjector injector;
    protected final InjectQueue injectQueue;

    public PacketProvider(InternalPacketListener highListener, InternalBufferListener lowListener, PacketInjector injector) {
        this.highListener = highListener;
        this.lowListener = lowListener;
        this.injector = injector;
        this.injectQueue = new InjectQueue();
    }

    public abstract void load();

    public abstract void init();

    public abstract void quit();

    public abstract void inject(final MCPlayer data);

    protected static class InjectQueue extends HashSet<UUID> {
        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return super.contains(o);
        }

        @Override
        public boolean add(UUID uuid) {
            return super.add(uuid);
        }

        @Override
        public boolean remove(Object o) {
            return super.remove(o);
        }
    }
}
