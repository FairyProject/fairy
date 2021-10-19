package io.fairyproject.bukkit.command;

import io.fairyproject.bean.Component;
import io.fairyproject.bukkit.command.util.CommandUtil;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandListener;

@Component
public class BukkitCommandListener implements CommandListener {

    @Override
    public void onCommandInitial(BaseCommand command, String[] alias) {
        final BukkitCommandExecutor bukkitCommandExecutor = new BukkitCommandExecutor(command, alias);
        CommandUtil.getCommandMap().register(bukkitCommandExecutor.getName(), bukkitCommandExecutor);
    }

}
