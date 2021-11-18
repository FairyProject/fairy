package io.fairyproject.gradle.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.FairyExtension;
import io.fairyproject.gradle.PlatformType;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@UtilityClass
public class VersionRetrieveUtil {

    private final String VERSION_URL = "https://maven.imanity.dev/service/rest/v1/search/assets?repository=imanity-libraries&group=io.fairyproject&name=<module>&sort=version&maven.extension=jar";

    public String getLatest(String module) throws IOException {
        final java.net.URL url = new URL(VERSION_URL.replace("<module>", module));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");

        final int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (InputStream in = connection.getInputStream()) {
                JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
                for (JsonElement items : jsonObject.getAsJsonArray("items")) {
                    final JsonObject itemObject = items.getAsJsonObject();

                    return itemObject.getAsJsonObject("maven2").get("version").getAsString();
                }
            }
        }

        return "0.5b1";
    }

    public void addExistingModule(FairyExtension extension, String from, @Nullable String version) throws IOException {
        if (from.contains("-") || from.startsWith("module.")) {
            // user specified platform
            if (isExistingModule(from)) {
                if (version == null) {
                    version = getLatest(from);
                }
                extension.getFairyModules().put(from, version);
                return;
            }
            throw new IllegalArgumentException("Couldn't find module " + from);
        }

        boolean found = false;
        for (PlatformType platformType : extension.getFairyPlatforms().get()) {
            if (isExistingModule(platformType.getDependencyName() + "-" + from)) {
                extension.getFairyModules().put(platformType.getDependencyName() + "-" + from, version == null ? getLatest(platformType.getDependencyName() + "-" + from) : version);
                found = true;
            }
        }

        if (!found) {
            if (isExistingModule("core-" + from)) {
                extension.getFairyModules().put("core-" + from, version == null ? getLatest("core-" + from) : version);
                return;
            }
        }

        throw new IllegalArgumentException("Couldn't find module " + from);
    }

    private boolean isExistingModule(String from) throws IOException {
        final java.net.URL url = new URL(VERSION_URL.replace("<module>", from));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");

        if (connection.getResponseCode() == 200) {
            return true;
        }
        return false;
    }

}
