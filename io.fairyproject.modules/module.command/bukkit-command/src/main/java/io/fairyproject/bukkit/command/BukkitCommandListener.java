package io.fairyproject.bukkit.command;

import io.fairyproject.bukkit.command.map.BukkitCommandMap;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandListener;
import io.fairyproject.container.InjectableComponent;
import lombok.RequiredArgsConstructor;

@InjectableComponent
@RequiredArgsConstructor
public class BukkitCommandListener implements CommandListener {

    private final BukkitCommandMap bukkitCommandMap;

    @Override
    public void onCommandInitial(BaseCommand command, String[] aliases) {
        bukkitCommandMap.register(command);
    }

    @Override
    public void onCommandRemoval(BaseCommand command) {
        bukkitCommandMap.unregister(command);
    }
}
