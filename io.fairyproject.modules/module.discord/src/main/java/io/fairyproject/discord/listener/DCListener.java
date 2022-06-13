package io.fairyproject.discord.listener;

import com.google.common.eventbus.EventBus;
import io.fairyproject.container.object.Obj;
import io.fairyproject.discord.DCBot;
import io.fairyproject.discord.event.DCMessageReceivedEvent;
import io.fairyproject.event.GlobalEventNode;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Obj
public class DCListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final DCBot bot = DCBot.from(event);
        if (bot.getNextMessageReader().handleMessage(event)) {
            // Don't process event if we processed next message
            return;
        }

        DCMessageReceivedEvent messageReceivedEvent = new DCMessageReceivedEvent(bot, event);
        GlobalEventNode.get().call(messageReceivedEvent);

        if (messageReceivedEvent.isCancelled()) {
            messageReceivedEvent.getMessage()
                    .delete()
                    .queue();
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        DCBot bot = DCBot.from(event);
        bot.getButtonReader().handle(event);
    }

}
