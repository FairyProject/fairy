package io.fairyproject.bukkit.command;

import io.fairyproject.bukkit.command.map.BukkitCommandMap;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandListener;
import io.fairyproject.command.CommandService;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PreInitialize;
import lombok.RequiredArgsConstructor;

@InjectableComponent
@RequiredArgsConstructor
public class BukkitCommandListener implements CommandListener {

    private final CommandService commandService;
    private final BukkitCommandMap bukkitCommandMap;

    @PreInitialize
    public void init() {
        commandService.addCommandListener(this);
    }

    @PostDestroy
    public void destroy() {
        commandService.removeCommandListener(this);
    }

    @Override
    public void onCommandInitial(BaseCommand command, String[] aliases) {
        bukkitCommandMap.register(command);
    }

    @Override
    public void onCommandRemoval(BaseCommand command) {
        bukkitCommandMap.unregister(command);
    }
}
