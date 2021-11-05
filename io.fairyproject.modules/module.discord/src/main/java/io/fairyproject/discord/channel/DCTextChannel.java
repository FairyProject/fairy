package io.fairyproject.discord.channel;

import io.fairyproject.discord.DCBot;
import io.fairyproject.discord.proxies.ProxyTextChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class DCTextChannel extends DCMessageChannel implements ProxyTextChannel {
    public DCTextChannel(MessageChannel original, DCBot bot) {
        super(original, bot);
        assert original instanceof TextChannel;
    }

    @Override
    public TextChannel original() {
        return (TextChannel) super.original();
    }
}
