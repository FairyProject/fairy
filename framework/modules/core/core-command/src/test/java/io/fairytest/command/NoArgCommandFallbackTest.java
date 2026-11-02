package io.fairytest.command;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandContext;
import io.fairyproject.command.CommandService;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoArgCommandFallbackTest extends JUnitJupiterBase {

    private TestCommand command;

    @BeforeEach
    public void setUp() {
        CommandService.INSTANCE.registerDefaultPresenceProvider(new TestPresenceProvider());
        command = new TestCommand();
        command.init();
    }

    @Test
    public void noArgumentsRunsNoArgCommand() {
        command.execute(new CommandContext(new String[0]));

        assertEquals(1, command.noArgCalls);
        assertEquals(0, command.helpCalls);
    }

    @Test
    public void subCommandIsStillFound() {
        command.execute(new CommandContext(new String[]{"sell"}));

        assertEquals(1, command.sellCalls);
        assertEquals(0, command.noArgCalls);
    }

    @Test
    public void unknownSubCommandShowsHelpInsteadOfRunningNoArgCommand() {
        command.execute(new CommandContext(new String[]{"selll"}));

        assertEquals(0, command.noArgCalls);
        assertEquals(1, command.helpCalls);
    }

    @Test
    public void extraArgumentsStillRunCommandWithoutSubCommands() {
        NoArgOnlyCommand noArgOnly = new NoArgOnlyCommand();
        noArgOnly.init();

        noArgOnly.execute(new CommandContext(new String[]{"foo"}));

        assertEquals(1, noArgOnly.noArgCalls);
        assertEquals(0, noArgOnly.helpCalls);
    }

    @Command("noarg")
    public static class NoArgOnlyCommand extends BaseCommand {

        private int noArgCalls;
        private int helpCalls;

        @Command("#")
        public void noArg(CommandContext context) {
            noArgCalls++;
        }

        @Override
        public void onHelp(CommandContext context) {
            helpCalls++;
        }

    }

    @Command("test")
    public static class TestCommand extends BaseCommand {

        private int noArgCalls;
        private int sellCalls;
        private int helpCalls;

        @Command("#")
        public void noArg(CommandContext context) {
            noArgCalls++;
        }

        @Command("sell")
        public void sell(CommandContext context) {
            sellCalls++;
        }

        @Override
        public void onHelp(CommandContext context) {
            helpCalls++;
        }

    }

}
