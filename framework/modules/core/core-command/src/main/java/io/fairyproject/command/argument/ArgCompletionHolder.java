package io.fairyproject.command.argument;

import io.fairyproject.command.CommandContext;

import java.util.Collection;

public interface ArgCompletionHolder {

    Collection<String> apply(CommandContext commandContext);

    String name();

}
