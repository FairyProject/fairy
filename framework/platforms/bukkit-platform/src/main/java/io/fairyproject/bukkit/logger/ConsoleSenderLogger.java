package io.fairyproject.bukkit.logger;

import io.fairyproject.log.ILogger;
import io.fairyproject.util.CC;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.ConsoleCommandSender;

@RequiredArgsConstructor
public class ConsoleSenderLogger implements ILogger {

    private final ConsoleCommandSender sender;
    private final ILogger errorLogger = new Log4jLogger();

    @Override
    public void info(String message, Object... replace) {
        sender.sendMessage(String.format(message, replace));
    }

    @Override
    public void debug(String message, Object... replace) {
        sender.sendMessage(CC.translate("&7[DEBUG] " + String.format(message, replace)));
    }

    @Override
    public void warn(String message, Object... replace) {
        sender.sendMessage(CC.translate("&e[WARN] " + String.format(message, replace)));
    }

    @Override
    public void error(String message, Object... replace) {
        sender.sendMessage(CC.translate("&c[ERROR] " + String.format(message, replace)));
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        sender.sendMessage(CC.translate("&7[INFO] " + String.format(message, replace)));
        errorLogger.error(throwable);
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        sender.sendMessage(CC.translate("&7[DEBUG] " + String.format(message, replace)));
        errorLogger.error(throwable);
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        sender.sendMessage(CC.translate("&e[WARN] " + String.format(message, replace)));
        errorLogger.error(throwable);
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        sender.sendMessage(CC.translate("&c[ERROR] " + String.format(message, replace)));
        errorLogger.error(throwable);
    }

    @Override
    public void info(Throwable throwable) {
        errorLogger.error(throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        errorLogger.error(throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        errorLogger.error(throwable);
    }

    @Override
    public void error(Throwable throwable) {
        errorLogger.error(throwable);
    }
}
