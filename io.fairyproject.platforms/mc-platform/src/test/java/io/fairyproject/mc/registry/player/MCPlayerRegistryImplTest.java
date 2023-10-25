/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.mc.registry.player;

import io.fairyproject.mc.MCPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class MCPlayerRegistryImplTest {

    private MCPlayerRegistryImpl registry;
    private MCPlayerPlatformOperator playerPlatformOperator;

    @BeforeEach
    public void setUp() {
        playerPlatformOperator = Mockito.mock(MCPlayerPlatformOperator.class);
        registry = new MCPlayerRegistryImpl(playerPlatformOperator);
    }

    @Test
    void onPostInitialize_shouldLoadAllExistingPlayers() {
        MCPlayer player1 = createFakePlayer();
        MCPlayer player2 = createFakePlayer();
        when(playerPlatformOperator.loadOnlinePlayers()).thenReturn(Arrays.asList(player1, player2));

        registry.onPostInitialize();

        assertEquals(2, registry.players.size());
        assertSame(player1, registry.players.get(player1.getUUID()));
        assertSame(player2, registry.players.get(player2.getUUID()));
    }

    @Nested
    public class AddPlayer {

        private MCPlayer player;

        @BeforeEach
        public void setUp() {
            player = Mockito.mock(MCPlayer.class);
            when(player.getUUID()).thenReturn(UUID.randomUUID());
        }

        @Test
        public void shouldAddToMap() {
            registry.addPlayer(player);

            assertTrue(registry.players.containsKey(player.getUUID()));
        }

        @Test
        public void ifPlayerAlreadyExist_shouldThrowException() {
            registry.addPlayer(player);

            assertThrows(IllegalArgumentException.class, () -> registry.addPlayer(player));
        }

    }

    @Nested
    public class RemovePlayer {

        private MCPlayer player;

        @BeforeEach
        public void setUp() {
            player = Mockito.mock(MCPlayer.class);
            when(player.getUUID()).thenReturn(UUID.randomUUID());
        }

        @Test
        public void shouldRemoveFromMap() {
            registry.addPlayer(player);
            registry.removePlayer(player);

            assertFalse(registry.players.containsKey(player.getUUID()));
        }

        @Test
        public void ifPlayerDoesNotExist_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> registry.removePlayer(player));
        }

    }

    @Nested
    public class FindPlayerByUuid {

        private MCPlayer player;

        @BeforeEach
        public void setUp() {
            player = Mockito.mock(MCPlayer.class);
            when(player.getUUID()).thenReturn(UUID.randomUUID());
        }

        @Test
        public void shouldReturnPlayer() {
            registry.addPlayer(player);

            assertEquals(player, registry.findPlayerByUuid(player.getUUID()));
        }

        @Test
        @Disabled
        public void ifPlayerDoesNotExist_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> registry.findPlayerByUuid(player.getUUID()));
        }

    }

    @Nested
    public class FindPlayerByName {

        private MCPlayer player;

        @BeforeEach
        public void setUp() {
            player = Mockito.mock(MCPlayer.class);
            when(player.getUUID()).thenReturn(UUID.randomUUID());
            when(player.getName()).thenReturn("test");
        }

        @Test
        public void shouldReturnPlayer() {
            registry.addPlayer(player);

            assertEquals(player, registry.findPlayerByName(player.getName()));
        }

        @Test
        public void ifPlayerDoesNotExist_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> registry.findPlayerByName(player.getName()));
        }

    }

    @Nested
    public class FindPlayerByPlatformPlayer {

        private MCPlayer player;
        private Object nativePlayer;

        @BeforeEach
        public void setUp() {
            UUID uuid = UUID.randomUUID();

            player = Mockito.mock(MCPlayer.class);
            when(player.getUUID()).thenReturn(uuid);
            when(player.getName()).thenReturn("test");

            nativePlayer = new Object();
            when(playerPlatformOperator.getUniqueId(nativePlayer)).thenReturn(uuid);
            when(playerPlatformOperator.getName(nativePlayer)).thenReturn("test");
        }

        @Test
        public void shouldReturnPlayer() {
            registry.addPlayer(player);

            assertEquals(player, registry.findPlayerByPlatformPlayer(nativePlayer));
        }

        @Test
        @Disabled
        public void ifPlayerDoesNotExist_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> registry.findPlayerByPlatformPlayer(nativePlayer));
        }
    }

    @Nested
    public class GetAllPlayers {

        private MCPlayer player1;
        private MCPlayer player2;

        @BeforeEach
        public void setUp() {
            player1 = createFakePlayer();
            player2 = createFakePlayer();
        }

        @Test
        public void shouldReturnAllPlayers() {
            registry.addPlayer(player1);
            registry.addPlayer(player2);

            assertEquals(2, registry.getAllPlayers().size());
        }

    }

    private MCPlayer createFakePlayer() {
        MCPlayer player = Mockito.mock(MCPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());

        return player;
    }


}