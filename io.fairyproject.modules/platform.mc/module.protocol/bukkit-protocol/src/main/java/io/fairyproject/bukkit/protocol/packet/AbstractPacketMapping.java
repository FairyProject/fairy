package io.fairyproject.bukkit.protocol.packet;

import io.fairyproject.bukkit.protocol.PacketBuilder;
import io.fairyproject.bukkit.protocol.PacketFactoryCreator;
import io.fairyproject.bukkit.protocol.PacketFactoryWrapper;
import io.fairyproject.bukkit.protocol.PacketMap;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.Packet;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPacketMapping<W, I> implements PacketMap<W, I> {
    private final Map<I, PacketFactoryWrapper<?, ?>> idToBuilders = new HashMap<>();
    private final Map<Class<? extends PacketWrapper<? extends W>>, PacketBuilder<? extends Packet, ? extends W>>  classToBuilders = new HashMap<>();

    protected <T extends PacketWrapper<K>, K extends W> PacketFactory<T, K> create(final I id, final Class<T> clazz) {
        return new PacketFactory<T, K>(id, clazz);
    }

    protected <T extends PacketWrapper<K>, K extends W> void create(final I id, Class<T> clazz, PacketBuilder<T, K> generator) {
        new PacketFactory<T, K>(id, clazz).create(generator);
    }

    @Override
    public <T extends PacketWrapper<K>, K extends W> Packet wrap(MCPlayer player, I id, W obj) {
        final PacketBuilder<T, K> factory = (PacketBuilder<T, K>) idToBuilders.get(id);
        return factory.wrap((K) obj, player);
    }

    class PacketFactory<T extends PacketWrapper<K>, K extends W> {
        private final I id;
        private final Class<T> wrapperType;

        public PacketFactory(I id, Class<T> wrapperType) {
            this.id = id;
            this.wrapperType = wrapperType;
        }

        private PacketFactoryCreator<T> creator;
        private PacketFactoryWrapper<T, K> wrapper;

        public PacketFactory<T, K> creator(PacketFactoryCreator<T> creator) {
            this.creator = creator;
            return this;
        }

        public PacketFactory<T, K> wrapper(PacketFactoryWrapper<T, K> wrapper) {
            this.wrapper = wrapper;
            return this;
        }

        public void create() {
            final PacketBuilder<T, K> builder = new PacketBuilder<T, K>() {
                @Override
                public T createEmpty() {
                    return creator.createEmpty();
                }

                @Override
                public T wrap(K typeObj, MCPlayer player) {
                    return wrapper.wrap(typeObj, player);
                }
            };

            create(builder);
        }

        public void create(final PacketBuilder<T, K> builder) {
            idToBuilders.put(id, builder);
            classToBuilders.put(wrapperType, builder);
        }
    }
    
}
