package io.fairyproject.discord.command;

import io.fairyproject.command.MessageType;
import io.fairyproject.command.PresenceProvider;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class DCPresenceProvider implements PresenceProvider<DCCommandContext> {
    @Override
    public Class<DCCommandContext> type() {
        return DCCommandContext.class;
    }

    @Override
    public void sendMessage(DCCommandContext commandContext, MessageType messageType, String... messages) {
        switch (messageType) {
            case INFO:
                commandContext.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("INFO")
                        .setDescription(String.join("\n", messages))
                        .setColor(Color.CYAN)
                        .build()
                ).queue();
                break;
            case WARN:
                commandContext.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("WARNING")
                        .setDescription(String.join("\n", messages))
                        .setColor(Color.ORANGE)
                        .build()
                ).queue();
                break;
            case ERROR:
                commandContext.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("ERROR")
                        .setDescription(String.join("\n", messages))
                        .setColor(Color.RED)
                        .build()
                ).queue();
                break;
        }
    }
}
