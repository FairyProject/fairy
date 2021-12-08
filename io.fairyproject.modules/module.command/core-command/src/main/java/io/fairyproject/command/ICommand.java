package io.fairyproject.command;

import java.util.List;

public interface ICommand {

    int getMaxParameterCount();

    int getRequireInputParameterCount();

    String getUsage(CommandContext commandContext);

    void execute(CommandContext commandContext);

    boolean canAccess(CommandContext commandContext);

    List<String> completeCommand(CommandContext commandContext);

    SubCommandType getSubCommandType();

}
