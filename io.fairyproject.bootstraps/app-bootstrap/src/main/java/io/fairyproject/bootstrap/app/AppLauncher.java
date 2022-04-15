package io.fairyproject.bootstrap.app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.app.console.FairyTerminalConsole;
import io.fairyproject.bootstrap.app.console.ForwardLogHandler;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class AppLauncher {

    private static final String FAIRY_JSON_PATH = "fairy.json";

    public static void main(String[] args) {
        initConsole();

        JsonObject jsonObject;
        try {
            jsonObject = new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(getResource(FAIRY_JSON_PATH))), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load " + FAIRY_JSON_PATH, throwable);
        }

        AppBootstrap bootstrap = new AppBootstrap();
        ApplicationHolder pluginHolder = new ApplicationHolder(jsonObject);
        AppBootstrap.INSTANCE = bootstrap;
        if (!bootstrap.load(pluginHolder.getPlugin())) {
            System.err.println("Failed to boot fairy! check stacktrace for the reason of failure!");
            System.exit(-1);
            return;
        }
        AppBootstrap.FAIRY_READY = true;

        pluginHolder.onLoad();

        bootstrap.enable();
        pluginHolder.onEnable();

        Thread shutdownHook = new Thread(() -> {
            try {
                io.fairyproject.Fairy.getPlatform().shutdown();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                try {
                    TerminalConsoleAppender.close();
                } catch (IOException e) {
                    // IGNORE
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        } else {
            try {
                URL url = AppLauncher.class.getClassLoader().getResource(filename);
                if (url == null) {
                    return null;
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    return connection.getInputStream();
                }
            } catch (IOException var4) {
                return null;
            }
        }
    }

    private static void initConsole() {
        Thread thread = new Thread("Console Thread") {
            @Override
            public void run() {
                new FairyTerminalConsole().start();
            }
        };

        java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
        global.setUseParentHandlers(false);
        for (java.util.logging.Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(new ForwardLogHandler());

        final Logger logger = LogManager.getRootLogger();

        System.setOut(org.apache.logging.log4j.io.IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream());
        System.setErr(org.apache.logging.log4j.io.IoBuilder.forLogger(logger).setLevel(Level.WARN).buildPrintStream());

        thread.setDaemon(true);
        thread.start();

        LogManager.getLogger(AppLauncher.class).info("Console initialized.");
    }

}
