package io.fairyproject.discord.button;

import com.google.common.collect.HashMultimap;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;

import java.util.function.BiConsumer;

public class ButtonReader {

    private final HashMultimap<String, BiConsumer<User, ButtonInteraction>> listening = HashMultimap.create();

    public void read(String id, BiConsumer<User, ButtonInteraction> consumer) {
        this.listening.put(id, consumer);
    }

    public void removeAll(String id) {
        this.listening.removeAll(id);
    }

    public void handle(ButtonClickEvent event) {
        for (BiConsumer<User, ButtonInteraction> consumer : this.listening.get(event.getComponentId())) {
            consumer.accept(event.getUser(), event.getInteraction());
        }
    }

}
