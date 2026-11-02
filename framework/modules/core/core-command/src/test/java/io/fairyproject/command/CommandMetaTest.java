package io.fairyproject.command;

import io.fairyproject.command.annotation.Arg;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.command.annotation.Completion;
import io.fairyproject.command.annotation.Wildcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandMetaTest {

    private CommandMeta commandMeta;

    @BeforeEach
    void setUp() throws Exception {
        BaseCommand baseCommand = Mockito.mock(BaseCommand.class);
        PresenceProvider<?> presenceProvider = Mockito.mock(PresenceProvider.class);
        Mockito.doReturn(CommandContext.class).when(presenceProvider).type();
        baseCommand.presenceProvider = presenceProvider;

        Method method = DummyCommand.class.getMethod("say", CommandContext.class, String.class);
        commandMeta = new CommandMeta(method.getAnnotation(Command.class), method, baseCommand);
    }

    @Test
    void completeFirstWordOfWildcard() {
        CommandContext context = new CommandContext(new String[]{"h"});

        assertEquals(Collections.singletonList("hello"), commandMeta.completeCommand(context));
    }

    @Test
    void completeWildcardBeyondParameterCount() {
        CommandContext context = new CommandContext(new String[]{"hello", "w"});

        assertEquals(Collections.singletonList("world"), commandMeta.completeCommand(context));
    }

    public static class DummyCommand {

        @Command("say")
        public void say(CommandContext context, @Arg("message") @Wildcard @Completion({"hello", "world"}) String message) {
            // only the signature is used
        }

    }

}
