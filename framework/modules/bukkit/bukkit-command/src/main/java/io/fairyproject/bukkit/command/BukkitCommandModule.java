package io.fairyproject.bukkit.command;

import io.fairyproject.container.*;
import io.fairyproject.bukkit.command.presence.DefaultPresenceProvider;
import io.fairyproject.command.CommandService;
import lombok.RequiredArgsConstructor;

@InjectableComponent
@RequiredArgsConstructor
public class BukkitCommandModule {

    private final CommandService commandService;

    @PreInitialize
    public void onPreInitialize() {
        commandService.registerDefaultPresenceProvider(new DefaultPresenceProvider());
    }
}
