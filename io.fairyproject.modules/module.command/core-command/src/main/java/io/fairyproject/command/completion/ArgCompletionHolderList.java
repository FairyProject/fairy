package io.fairyproject.command.completion;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandContext;
import io.fairyproject.command.argument.ArgCompletionHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ArgCompletionHolderList implements ArgCompletionHolder {
    private final MethodHandle methodHandle;
    private final BaseCommand baseCommand;

    private final boolean hasParameter;
    private final String name;

    @Override
    @SneakyThrows
    public Collection<String> apply(CommandContext commandContext) {
        if (this.hasParameter) {
            return (List<String>) methodHandle.invoke(baseCommand, commandContext);
        } else {
            return (List<String>) methodHandle.invoke(baseCommand);
        }
    }

    @Override
    public String name() {
        return this.name;
    }
}
