package io.fairyproject.command.completion;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandContext;
import io.fairyproject.command.argument.ArgCompletionHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Collection;

@RequiredArgsConstructor
public class ArgCompletionHolderStringArray implements ArgCompletionHolder {

    private final MethodHandle methodHandle;
    private final BaseCommand baseCommand;

    private final boolean hasParameter;
    private final String name;

    @Override
    @SneakyThrows
    public Collection<String> apply(CommandContext commandContext) {
        if (this.hasParameter) {
            return Arrays.asList((String[]) methodHandle.invoke(baseCommand, commandContext));
        } else {
            return Arrays.asList((String[]) methodHandle.invoke(baseCommand));
        }
    }

    @Override
    public String name() {
        return this.name;
    }
}
