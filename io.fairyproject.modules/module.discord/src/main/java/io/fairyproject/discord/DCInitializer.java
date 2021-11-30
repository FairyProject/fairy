package io.fairyproject.discord;

import io.fairyproject.container.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Service
public class DCInitializer {

    @Getter
    private List<Object> listeners;

    @PreInitialize
    public void onPreInitialize() {
        this.listeners = new ArrayList<>();
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(net.dv8tion.jda.api.hooks.EventListener.class)
                .onEnable(obj -> {
                    this.listeners.add(obj);
                    DCBot.all().forEach(bot -> bot.addEventListener(obj));
                })
                .onDisable(obj -> {
                    this.listeners.remove(obj);
                    DCBot.all().forEach(bot -> bot.removeEventListener(obj));
                })
                .build());
    }

}