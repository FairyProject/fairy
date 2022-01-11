package io.fairyproject.mc.protocol.mapping;

import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.packet.PacketPlay;

public class MCProtocolMapping1_18 extends MCProtocolMapping {

    public MCProtocolMapping1_18() {
        // Play
        this.registerProtocol(0, new AbstractProtocol() {
            @Override
            public void init() {
                this.registerOut(54, PacketPlay.Out.PlayerInfo.class);
                this.registerOut(83, PacketPlay.Out.ScoreboardObjective.class);
                this.registerOut(86, PacketPlay.Out.ScoreboardScore.class);
                this.registerOut(76, PacketPlay.Out.ScoreboardDisplayObjective.class);
                this.registerOut(85, PacketPlay.Out.ScoreboardTeam.class);
                this.registerOut(88, PacketPlay.Out.SubTitle.class);
                this.registerOut(90, PacketPlay.Out.Title.class);
                this.registerOut(91, PacketPlay.Out.TitleTimes.class);
                this.registerOut(16, PacketPlay.Out.TitleClear.class);

                this.registerOut(95, PacketPlay.Out.Tablist.class);
            }
        });
    }

    @Override
    public MCVersion getVersion() {
        return MCVersion.V1_18;
    }
}
