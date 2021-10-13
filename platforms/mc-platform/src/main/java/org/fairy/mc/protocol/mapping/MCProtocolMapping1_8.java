package org.fairy.mc.protocol.mapping;

import org.fairy.mc.protocol.MCVersion;

public class MCProtocolMapping1_8 extends MCProtocolMapping {

    public MCProtocolMapping1_8() {
        // Handshake
        this.registerProtocol(-1, new AbstractProtocol() {
            @Override
            public void init() {

            }
        });

        // Play
        this.registerProtocol(0, new AbstractProtocol() {
            @Override
            public void init() {
                this.registerOut();
            }
        });

        // Status
        this.registerProtocol(1, new AbstractProtocol() {
            @Override
            public void init() {

            }
        });

        // Login
        this.registerProtocol(2, new AbstractProtocol() {
            @Override
            public void init() {

            }
        });
    }

    @Override
    public MCVersion getVersion() {
        return MCVersion.v1_8;
    }
}
