package io.fairytest.command;

import io.fairyproject.command.CommandContext;
import io.fairyproject.command.MessageType;
import io.fairyproject.command.PresenceProvider;

public class TestPresenceProvider implements PresenceProvider<CommandContext> {

    @Override
    public Class<CommandContext> type() {
        return CommandContext.class;
    }

    @Override
    public void sendMessage(CommandContext commandContext, MessageType messageType, String... messages) {
        // messages are not asserted in these tests
    }

}
