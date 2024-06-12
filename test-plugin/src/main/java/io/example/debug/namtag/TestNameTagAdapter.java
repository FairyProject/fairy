package io.example.debug.namtag;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.nametag.NameTag;
import io.fairyproject.mc.nametag.NameTagAdapter;
import io.fairyproject.mc.nametag.NameTagService;
import io.fairyproject.mc.scheduler.MCSchedulers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@InjectableComponent
public class TestNameTagAdapter extends NameTagAdapter {

    public TestNameTagAdapter(NameTagService service) {
        super("test", 0);

        MCSchedulers.getGlobalScheduler().scheduleAtFixedRate(service::updateAll, 20L, 20L);
    }

    @Override
    public NameTag fetch(MCPlayer player, MCPlayer target) {
        return new NameTag(Component.text("HELLO"), Component.empty(), NamedTextColor.WHITE, WrapperPlayServerTeams.NameTagVisibility.ALWAYS);
    }
}
