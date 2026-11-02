package io.fairytest.command;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandContext;
import io.fairyproject.command.CommandService;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubCommandCompletionTest extends JUnitJupiterBase {

    private TestCommand command;

    @BeforeEach
    public void setUp() {
        CommandService.INSTANCE.registerDefaultPresenceProvider(new TestPresenceProvider());
        command = new TestCommand();
        command.init();
    }

    @Test
    public void allSubCommandNamesAreSortedWithNoArgCommand() {
        assertEquals(Arrays.asList("buy", "sell", "sellall"), complete(""));
    }

    @Test
    public void partialNameIsCompletedWithNoArgCommand() {
        assertEquals(Arrays.asList("sell", "sellall"), complete("se"));
    }

    @Test
    public void longerNameIsCompletedAfterExactMatch() {
        assertEquals(Arrays.asList("sell", "sellall"), complete("sell"));
    }

    @Test
    public void unknownNameCompletesNothing() {
        assertEquals(Collections.emptyList(), complete("x"));
    }

    private List<String> complete(String... args) {
        return command.completeCommand(new CommandContext(args));
    }

    @Command("test")
    public static class TestCommand extends BaseCommand {

        @Command("#")
        public void noArg(CommandContext context) {
            // only the completion is tested
        }

        @Command("sell")
        public void sell(CommandContext context) {
            // only the completion is tested
        }

        @Command("sellall")
        public void sellAll(CommandContext context) {
            // only the completion is tested
        }

        @Command("buy")
        public void buy(CommandContext context) {
            // only the completion is tested
        }

    }

}
