package io.fairyproject.mc.protocol.component;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.fairyproject.container.ComponentHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketListenerComponentHolder extends ComponentHolder {

    private final Map<Class<?>, PacketListenerCommon> listenerCommonMap = new ConcurrentHashMap<>();

    @Override
    public Class<?>[] type() {
        return new Class[] {PacketListener.class, PacketListenerCommon.class};
    }

    @Override
    public void onEnable(Object obj) {
        if (obj instanceof PacketListenerCommon) {
            PacketEvents.getAPI().getEventManager().registerListener((PacketListenerCommon) obj);
        } else {
            // TODO: made it configurable?
            final PacketListenerAbstract listener = ((PacketListener) obj).asAbstract(PacketListenerPriority.NORMAL, false, true);
            PacketEvents.getAPI().getEventManager().registerListener(listener);

            this.listenerCommonMap.put(obj.getClass(), listener);
        }
    }

    @Override
    public void onDisable(Object obj) {
        if (obj instanceof PacketListenerCommon) {
            PacketEvents.getAPI().getEventManager().unregisterListener((PacketListenerCommon) obj);
        } else {
            final PacketListenerCommon listenerCommon = this.listenerCommonMap.remove(obj.getClass());
            if (listenerCommon != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(listenerCommon);
            }
        }
    }
}
