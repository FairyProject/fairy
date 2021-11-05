package io.fairyproject.discord;

import io.fairyproject.Fairy;
import io.fairyproject.bean.*;
import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryRepository;
import io.fairyproject.module.Modular;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Modular(
        value = "core-discord"
)
@Service(name = "discord-initializer")
public class DCInitializer {

    private static final LibraryRepository REPOSITORY = new LibraryRepository("https://m2.dv8tion.net/releases");
    private static final Library JDA = Library.builder()
            .gradle("net.dv8tion:JDA:4.3.0_339")
            .repository(REPOSITORY)
            .build();

    @Getter
    private List<Object> listeners;
    @Getter
    private boolean ready;

    @PreInitialize
    public void onPreInitialize() {
        this.ready = false;
        Fairy.getLibraryHandler().downloadLibraries(true, JDA);

        this.listeners = new ArrayList<>();
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(net.dv8tion.jda.api.hooks.EventListener.class)
                .onEnable(obj -> {
                    this.listeners.add(obj);
                    if (this.ready) {
                        DCBot.all().forEach(bot -> bot.addEventListener(obj));
                    }
                })
                .onDisable(obj -> {
                    this.listeners.remove(obj);
                    if (this.ready) {
                        DCBot.all().forEach(bot -> bot.removeEventListener(obj));
                    }
                })
                .build());
    }

    @PostInitialize
    public void onPostInitialize() {
        this.ready = true;
    }

}
