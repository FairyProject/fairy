package io.fairyproject.command.argument;

import io.fairyproject.command.CommandContext;

import java.util.Collection;

public interface ArgTabCompletionHolder {

    Collection<String> apply(CommandContext commandContext);

}
