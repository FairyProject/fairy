package io.fairyproject.bukkit.command;

import io.fairyproject.container.ContainerConstruct;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.bukkit.command.presence.DefaultPresenceProvider;
import io.fairyproject.command.CommandService;

@Service(name = "bukkit:command", depends = CommandService.class)
public class BukkitCommandModule {
    private final ContainerContext containerContext;

    @ContainerConstruct
    public BukkitCommandModule(ContainerContext containerContext) {
        this.containerContext = containerContext;
    }

    @PreInitialize
    public void preInit() {
        CommandService commandService = (CommandService) this.containerContext.getBean(CommandService.class);
        commandService.registerDefaultPresenceProvider(new DefaultPresenceProvider());
    }
}
