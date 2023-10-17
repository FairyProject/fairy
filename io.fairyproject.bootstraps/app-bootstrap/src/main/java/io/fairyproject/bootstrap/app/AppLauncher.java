package io.fairyproject.bootstrap.app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.fairyproject.bootstrap.PluginClassInitializerFinder;
import io.fairyproject.bootstrap.internal.FairyInternalIdentityMeta;
import io.fairyproject.plugin.initializer.PluginClassInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

@FairyInternalIdentityMeta
public class AppLauncher {

    private static final String FAIRY_JSON_PATH = "fairy.json";

    public static void main(String[] args) {
        JsonObject jsonObject;
        try {
            jsonObject = new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(getResource(FAIRY_JSON_PATH))), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load " + FAIRY_JSON_PATH, throwable);
        }

        AppBootstrap bootstrap = new AppBootstrap();
        AppBootstrap.INSTANCE = bootstrap;
        if (!bootstrap.preload()) {
            System.err.println("Failed to boot fairy! check stacktrace for the reason of failure!");
            System.exit(-1);
            return;
        }

        PluginClassInitializer pluginClassInitializer = PluginClassInitializerFinder.find();
        ApplicationHolder pluginHolder = new ApplicationHolder(pluginClassInitializer, jsonObject);
        bootstrap.load(pluginHolder.getPlugin());
        AppBootstrap.FAIRY_READY = true;

        pluginHolder.onLoad();

        bootstrap.enable();
        pluginHolder.onEnable();

        Thread shutdownHook = new Thread(() -> {
            try {
                io.fairyproject.Fairy.getPlatform().shutdown();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
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

}
