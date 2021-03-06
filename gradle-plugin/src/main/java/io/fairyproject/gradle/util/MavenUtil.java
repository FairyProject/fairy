package io.fairyproject.gradle.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.FairyExtension;
import io.fairyproject.gradle.FairyPlugin;
import io.fairyproject.gradle.IDEDependencyLookup;
import io.fairyproject.shared.FairyVersion;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public class MavenUtil {

    private final String VERSION_URL = "https://maven.imanity.dev/service/rest/v1/search/assets?repository=imanity-libraries&group=<group>&name=<module>&sort=version&maven.extension=jar";
    private final String ITEM_URL = "https://maven.imanity.dev/service/rest/v1/search?repository=imanity-libraries&group=io.fairyproject&name=<module>";

    public String getLatest(String module) throws IOException {
        if (FairyPlugin.IS_IN_IDE) {
            final String identityPath = IDEDependencyLookup.getIdentityPath(module);

            return (String) FairyPlugin.INSTANCE.getProject().project(identityPath).getVersion();
        }
        String latest = getLatest("io.fairyproject", module);
        if (FairyVersion.SNAPSHOT_INDIVIDUAL_PATTERN.matcher(latest).find()) {
            latest = latest.split("-")[0] + "-SNAPSHOT";
        }
        return latest;
    }

    public String getLatest(String group, String artifact) throws IOException {
        final URL url = new URL(VERSION_URL
                .replace("<module>", artifact)
                .replace("<group>", group)
        );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

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

        return "0.5.2b3";
    }

    public void addExistingModule(FairyExtension extension, String from, @Nullable String version) throws IOException {
        if (version == null) {
            version = getLatest(from);
        }
        extension.getFairyModules().put(from, version);
    }

    public boolean isExistingModule(String from) throws IOException {
        if (FairyPlugin.IS_IN_IDE) {
            return IDEDependencyLookup.getIdentityPath(from) != null;
        }
        final java.net.URL url = new URL(ITEM_URL.replace("<module>", from));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");

        if (connection.getResponseCode() == 200) {
            final JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
            return jsonObject.get("items").getAsJsonArray().size() > 0;
        }
        return false;
    }

}
