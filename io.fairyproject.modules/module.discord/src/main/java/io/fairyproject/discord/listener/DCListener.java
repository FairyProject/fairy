package io.fairyproject.discord.listener;

import io.fairyproject.bean.Component;
import io.fairyproject.discord.DCBot;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Component
public class DCListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final DCBot bot = DCBot.from(event);
        bot.getNextMessageReader().handleMessage(event);
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        super.onButtonClick(event);
    }
}
