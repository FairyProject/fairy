package io.fairyproject.discord;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.container.collection.ContainerObjCollector;
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
        ContainerContext.get().objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(net.dv8tion.jda.api.hooks.EventListener.class))
                .withAddHandler(ContainerObjCollector.warpInstance(net.dv8tion.jda.api.hooks.EventListener.class, obj -> {
                    this.listeners.add(obj);
                    DCBot.all().forEach(bot -> bot.addEventListener(obj));
                }))
                .withRemoveHandler(ContainerObjCollector.warpInstance(net.dv8tion.jda.api.hooks.EventListener.class, obj -> {
                    this.listeners.remove(obj);
                    DCBot.all().forEach(bot -> bot.removeEventListener(obj));
                }))
        );
    }

}