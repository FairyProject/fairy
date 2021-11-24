package io.fairyproject.discord.command;

import io.fairyproject.command.CommandContext;
import io.fairyproject.discord.DCBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

@Getter
public class DCCommandContext extends CommandContext {

    private final DCBot bot;
    private final MessageChannel channel;
    private final User author;

    public DCCommandContext(String[] args, DCBot bot, MessageChannel channel, User author) {
        super(args);
        this.bot = bot;
        this.channel = channel;
        this.author = author;
    }

    @Override
    public String name() {
        return "discord user/bot";
    }
}
