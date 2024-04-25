package io.fairyproject.command.argument;

import io.fairyproject.command.CommandContext;
import io.fairyproject.command.parameter.ArgTransformer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ArgProperty<T> {

    public static <T> ArgProperty<T> create(String key, Class<T> type) {
        return new ArgProperty<>(key, type);
    }

    private final String key;
    private final Class<T> type;
    private Consumer<CommandContext> missingArgument;
    private UnknownArgumentHandler unknownArgument;
    private ArgTransformer<T> parameterHolder;

    public ArgProperty<T> onMissingArgument(Consumer<CommandContext> missingArgument) {
        this.missingArgument = missingArgument;
        return this;
    }

    public ArgProperty<T> onUnknownArgument(UnknownArgumentHandler unknownArgument) {
        this.unknownArgument = unknownArgument;
        return this;
    }

    public ArgProperty<T> parameterHolder(ArgTransformer<T> parameterHolder) {
        this.parameterHolder = parameterHolder;
        return this;
    }

    public T cast(Object object) {
        return this.type.cast(object);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgProperty<?> that = (ArgProperty<?>) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public interface UnknownArgumentHandler {

        void accept(CommandContext commandContext, String source, String reason);

    }
}
