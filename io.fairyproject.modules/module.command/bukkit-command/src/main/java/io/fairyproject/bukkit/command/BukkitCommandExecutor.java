package io.fairyproject.bukkit.command;

import io.fairyproject.bukkit.command.event.BukkitCommandContext;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandContext;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class BukkitCommandExecutor extends Command {

    private final BaseCommand command;
    private final String fallbackPrefix;

    public BukkitCommandExecutor(BaseCommand command, String[] name) {
        super(name[0]);
        this.command = command;

        if (name.length > 1) {
            this.setAliases(Arrays.asList(Arrays.copyOfRange(name, 1, name.length)));
        }

        final JavaPlugin javaPlugin = JavaPluginUtil.getProvidingPlugin(command.getClass());
        if (javaPlugin != null) {
            this.fallbackPrefix = javaPlugin.getName();
        } else {
            this.fallbackPrefix = "Fairy";
        }
    }

    @Override
    public @NotNull String getDescription() {
        return this.command.getDescription();
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String mainCommand, String[] args) {
        CommandContext commandContext = new BukkitCommandContext(commandSender, args);
        try {
            this.command.execute(commandContext);
        } catch (Throwable throwable) {
            this.command.onError(commandContext, throwable);
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {
        CommandContext commandContext = new BukkitCommandContext(sender, args);
        try {
            return this.command.completeCommand(commandContext);
        } catch (Throwable throwable) {
            this.command.onError(commandContext, throwable);
            return Collections.emptyList();
        }
    }
}
