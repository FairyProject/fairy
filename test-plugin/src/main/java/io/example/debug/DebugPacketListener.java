package io.example.debug;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import io.fairyproject.container.InjectableComponent;

@InjectableComponent
public class DebugPacketListener extends PacketListenerAbstract {

    public DebugPacketListener() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.TEAMS) {
            WrapperPlayServerTeams wrapperPlayServerTeams = new WrapperPlayServerTeams(event);

//            if (wrapperPlayServerTeams.getTeamMode() == WrapperPlayServerTeams.TeamMode.ADD_ENTITIES)
//                event.setCancelled(true);

        }
    }
}
