package io.fairyproject.discord.button;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ButtonReader {

    private final Map<String, Set<BiConsumer<User, ButtonInteraction>>> listening = new ConcurrentHashMap<>();

    public void read(String id, BiConsumer<User, ButtonInteraction> consumer) {
        this.listening.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(consumer);
    }

    public void removeAll(String id) {
        this.listening.remove(id);
    }

    public void handle(ButtonClickEvent event) {
        for (BiConsumer<User, ButtonInteraction> consumer : this.listening.get(event.getComponentId())) {
            consumer.accept(event.getUser(), event.getInteraction());
        }
    }

}
