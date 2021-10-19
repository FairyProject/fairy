package io.fairyproject.bukkit.command;

import io.fairyproject.bukkit.command.event.BukkitCommandContext;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandContext;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BukkitCommandExecutor extends Command {

    private final BaseCommand command;

    protected BukkitCommandExecutor(BaseCommand command, String[] name) {
        super(name[0]);
        this.command = command;

        if (name.length > 1) {
            this.setAliases(Arrays.asList(Arrays.copyOfRange(name, 1, name.length - 1)));
        }
    }

    @Override
    public String getDescription() {
        return this.command.getDescription();
    }

    @Override
    public boolean execute(CommandSender commandSender, String mainCommand, String[] args) {
        CommandContext commandContext = new BukkitCommandContext(commandSender, args);
        this.command.evalCommand(commandContext);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        CommandContext commandContext = new BukkitCommandContext(sender, args);
        final List<String> list = new ArrayList<>(this.command.completeCommand(commandContext));
        list.addAll(this.command.getCommandsForCompletion(commandContext));
        return list;
    }
}
