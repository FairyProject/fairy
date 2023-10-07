package io.fairyproject.bukkit.command;

import io.fairyproject.container.*;
import io.fairyproject.bukkit.command.presence.DefaultPresenceProvider;
import io.fairyproject.command.CommandService;

@InjectableComponent
@DependsOn(CommandService.class)
public class BukkitCommandModule {
    private final ContainerContext containerContext;

    @ContainerConstruct
    public BukkitCommandModule(ContainerContext containerContext) {
        this.containerContext = containerContext;
    }

    @PreInitialize
    public void preInit() {
        CommandService commandService = (CommandService) this.containerContext.getSingleton(CommandService.class);
        commandService.registerDefaultPresenceProvider(new DefaultPresenceProvider());
    }
}
