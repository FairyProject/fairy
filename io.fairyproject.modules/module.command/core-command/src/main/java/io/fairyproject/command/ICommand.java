package io.fairyproject.command;

import java.util.List;

public interface ICommand {

    int order();

    int getMaxParameterCount();

    int getRequireInputParameterCount();

    boolean isDisplayOnPermission();

    String getUsage(CommandContext commandContext);

    void execute(CommandContext commandContext);

    boolean canAccess(CommandContext commandContext);

    List<String> completeCommand(CommandContext commandContext);

    SubCommandType getSubCommandType();

}
