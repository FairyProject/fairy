package io.fairyproject.discord.command;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandListener;
import io.fairyproject.command.argument.ArgProperty;
import io.fairyproject.discord.DCBot;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class DCCommandListener implements CommandListener {

    @Override
    public void onCommandInitial(BaseCommand command, String[] alias) {
    }

    @Override
    public void onCommandRemoval(BaseCommand command) {

    }

    private CommandData fromBaseCommand(BaseCommand command) {
        CommandData commandData = new CommandData(command.getCommandPrefix(), command.getDescription());
        for (ArgProperty<?> baseArg : command.getBaseArgs()) {
            commandData.addOption(this.from(baseArg.getType()), baseArg.getKey(), baseArg.getDescription(), true);
        }

        commandData.addSubcommands()
    }

    private OptionType from(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return OptionType.BOOLEAN;
        } else if (type == int.class || type == long.class || type == short.class || type == byte.class || Number.class.isAssignableFrom(type)) {
            return OptionType.NUMBER;
        } else if (User.class.isAssignableFrom(type)) {
            return OptionType.USER;
        } else if (MessageChannel.class.isAssignableFrom(type)) {
            return OptionType.CHANNEL;
        } else if (Role.class.isAssignableFrom(type)) {
            return OptionType.ROLE;
        } else if (IMentionable.class.isAssignableFrom(type)) {
            return OptionType.MENTIONABLE;
        }
        return OptionType.STRING;
    }
}
