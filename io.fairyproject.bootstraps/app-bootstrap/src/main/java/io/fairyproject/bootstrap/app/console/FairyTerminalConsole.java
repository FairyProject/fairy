package io.fairyproject.bootstrap.app.console;

import io.fairyproject.bootstrap.app.AppBootstrap;
import net.minecrell.terminalconsole.SimpleTerminalConsole;

public class FairyTerminalConsole extends SimpleTerminalConsole {

    @Override
    protected boolean isRunning() {
        if (AppBootstrap.FAIRY_READY) {
            return io.fairyproject.Fairy.isRunning();
        }
        return true;
    }

    @Override
    protected void runCommand(String command) {
        if (AppBootstrap.FAIRY_READY) {
            // TODO
        }
    }

    @Override
    protected void shutdown() {
        if (AppBootstrap.FAIRY_READY) {
            io.fairyproject.Fairy.getPlatform().shutdown();
            return;
        }
        System.exit(-1);
    }
}
