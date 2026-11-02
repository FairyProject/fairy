package io.fairyproject.command;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.fairyproject.bukkit.command.BukkitCommandExecutor;
import io.fairyproject.bukkit.plugin.impl.RootJavaPluginIdentifier;
import io.fairyproject.bukkit.plugin.impl.SpecifyJavaPluginIdentifier;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

class BukkitCommandExecutorTest {

    private BaseCommand command;
    private CommandSender sender;
    private BukkitCommandExecutor executor;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RootJavaPluginIdentifier.getInstance().addFirst(new SpecifyJavaPluginIdentifier(MockBukkit.createMockPlugin("test-plugin")));

        command = Mockito.mock(BaseCommand.class);
        sender = Mockito.mock(CommandSender.class);
        executor = new BukkitCommandExecutor(command, new String[]{"test"});
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
        RootJavaPluginIdentifier.clearInstance();
    }

    @Test
    void permissionFollowsCommandAccess() {
        Mockito.when(command.canAccess(any())).thenReturn(false);
        assertFalse(executor.testPermissionSilent(sender));

        Mockito.when(command.canAccess(any())).thenReturn(true);
        assertTrue(executor.testPermissionSilent(sender));
    }

    @Test
    void presenceProviderIsAppliedBeforeCheckingAccess() {
        Mockito.when(command.canAccess(any())).thenReturn(true);

        assertTrue(executor.testPermissionSilent(sender));

        InOrder inOrder = Mockito.inOrder(command);
        inOrder.verify(command).applyPresenceProvider(any());
        inOrder.verify(command).canAccess(any());
    }

    @Test
    void tabCompleteErrorIsNotSentToSender() {
        Mockito.when(command.completeCommand(any())).thenThrow(new IndexOutOfBoundsException("broken completion"));

        assertTrue(executor.tabComplete(sender, "test", new String[]{"a"}).isEmpty());
        Mockito.verify(command, Mockito.never()).onError(any(), any());
    }

}
