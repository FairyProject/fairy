package io.fairyproject.mc.protocol.item;

import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
@Data
public class PlayerInfoData {

    private final int ping;
    private final MCGameProfile gameProfile;
    private final GameMode gameMode;
    private final Component component;

    public static PlayerInfoData create(MCPlayer mcPlayer) {
        return new PlayerInfoData(mcPlayer.getPing(), mcPlayer.getGameProfile(), mcPlayer.getGameMode(), mcPlayer.getDisplayName());
    }

}
