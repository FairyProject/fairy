package io.fairyproject.discord.button;

import com.google.common.collect.HashMultimap;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;

import java.util.function.BiConsumer;

public class ButtonReader {

    private final HashMultimap<String, BiConsumer<User, ButtonInteraction>> listening = HashMultimap.create();

    public void addListening(String id, BiConsumer<User, ButtonInteraction> listening) {
        this.listening.put(id, listening);
    }

}
