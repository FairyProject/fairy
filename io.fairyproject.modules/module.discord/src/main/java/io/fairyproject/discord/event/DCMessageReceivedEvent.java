package io.fairyproject.discord.event;

import io.fairyproject.discord.DCBot;
import io.fairyproject.event.Cancellable;
import io.fairyproject.event.Event;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

@Event
public class DCMessageReceivedEvent implements Cancellable {

    @Getter
    private final DCBot bot;
    private final MessageReceivedEvent event;

    public DCMessageReceivedEvent(DCBot bot, MessageReceivedEvent event) {
        this.bot = bot;
        this.event = event;
    }

    public Message getMessage()
    {
        return this.event.getMessage();
    }

    public User getAuthor()
    {
        return this.event.getAuthor();
    }

    @Nullable
    public Member getMember()
    {
        return this.event.getMember();
    }

    public MessageChannel getChannel()
    {
        return this.event.getChannel();
    }

    public Guild getGuild()
    {
        return this.event.getGuild();
    }

}
