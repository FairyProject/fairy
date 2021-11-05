package io.fairyproject.discord;

import io.fairyproject.discord.proxies.ProxyGuild;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;

@RequiredArgsConstructor
public class DCGuild implements ProxyGuild {

    private final Guild guild;
    private final DCBot bot;

    @Override
    public Guild original() {
        return this.guild;
    }

}
