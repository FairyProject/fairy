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

package io.fairyproject.bukkit.version;

import io.fairyproject.mc.version.MCVersion;
import org.bukkit.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BukkitVersionDecoderImplTest {

    @Test
    public void decode_shouldParseCorrectly() {
        Server server = Mockito.mock(Server.class);
        Mockito.when(server.getVersion()).thenReturn("Test (MC: 1.17.1)");
        BukkitVersionDecoder decoder = new BukkitVersionDecoderImpl();
        MCVersion version = decoder.decode(server);

        Assertions.assertEquals(MCVersion.of(1, 17, 1), version);
    }

    @Test
    public void unknownVersion_shouldThrowException() {
        Server server = Mockito.mock(Server.class);
        Mockito.when(server.getVersion()).thenReturn("Unknown thing kek");
        BukkitVersionDecoder decoder = new BukkitVersionDecoderImpl();

        Assertions.assertThrows(IllegalArgumentException.class, () -> decoder.decode(server));
    }

}
