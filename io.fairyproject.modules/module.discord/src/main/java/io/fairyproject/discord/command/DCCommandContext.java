package io.fairyproject.discord.command;

import io.fairyproject.command.CommandContext;
import io.fairyproject.discord.DCBot;
import io.fairyproject.discord.channel.DCMessageChannel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

@Getter
public class DCCommandContext extends CommandContext {

    private final DCBot bot;
    private final DCMessageChannel channel;
    private final User author;
    @Setter
    private String commandPrefix = DCBot.DEFAULT_COMMAND_PREFIX;

    public DCCommandContext(String[] args, DCBot bot, DCMessageChannel channel, User author) {
        super(args);
        this.bot = bot;
        this.channel = channel;
        this.author = author;
    }

    @Override
    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    @Override
    public String name() {
        return "discord user/bot";
    }
}
