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

package io.fairyproject.tests.mc.configuration;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.configuration.TestConfiguration;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.PacketEventsBuilder;
import io.fairyproject.mc.protocol.packet.PacketSender;
import io.fairyproject.tests.mc.protocol.PacketSenderMock;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.tests.mc.protocol.MockPacketEventsBuilder;

@TestConfiguration
public class MCProtocolTestConfiguration {

    @InjectableComponent
    public PacketEventsBuilder providePacketEventsBuilder(
            MCServer mcServer,
            MCVersionMappingRegistry versionMappingRegistry
    ) {
        return new MockPacketEventsBuilder(mcServer, versionMappingRegistry);
    }

    @InjectableComponent
    public PacketSender providePacketSender() {
        return new PacketSenderMock();
    }

}
