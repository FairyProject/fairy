package io.fairyproject.bukkit.visual;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import io.fairyproject.bukkit.visual.sender.VisualBlockSender;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.mc.protocol.MCProtocol;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@InjectableComponent
@RequiredArgsConstructor
public class VisualPacketListener extends PacketListenerAbstract {

    private final VisualBlockService visualBlockService;

    @PostInitialize
    public void onPostInitialize() {
        MCProtocol.listen(this);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        Player player = event.getPlayer();
        if (player == null)
            return;

        if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            handleBlockDigging(event, player);
        } else if (packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            handleBlockPlace(event, player);
        }
    }

    private void handleBlockPlace(PacketReceiveEvent event, Player player) {
        WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);

        Vector3i blockPosition = packet.getBlockPosition();
        if (visualBlockService.isVisualBlock(player, blockPosition.x, blockPosition.y, blockPosition.z)) {
            event.setCancelled(true);
        }
    }

    private void handleBlockDigging(PacketReceiveEvent event, Player player) {
        VisualBlockSender sender = visualBlockService.getVisualBlockSender();
        WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);

        Vector3i blockPosition = packet.getBlockPosition();
        VisualBlock visualBlock = visualBlockService.getVisualBlock(player, blockPosition.x, blockPosition.y, blockPosition.z);
        if (visualBlock != null) {
            event.setCancelled(true);

            sender.sendBlock(player, visualBlock);
        }
    }
}
