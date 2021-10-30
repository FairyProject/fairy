package io.fairyproject.gradle.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public class VersionRetrieveUtil {

    private final String URL = "https://maven.imanity.dev/service/rest/v1/search/assets?repository=imanity-libraries&group=io.fairyproject&name=framework&sort=version&maven.classifier=core-platform&sort=version&maven.extension=jar";

    public String getLatest() throws IOException {
        final java.net.URL url = new URL(URL); // TODO - ensure url correctly
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

}
