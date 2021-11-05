package io.fairyproject.bootstrap.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class DownloadUtil {

    private final String URL = "https://maven.imanity.dev/service/rest/v1/search/assets/download?sort=version&repository=imanity-libraries&maven.groupId=io.fairyproject&maven.artifactId=framework&maven.classifier=";

    @SuppressWarnings("Duplicates")
    public Path download(Path path, String core) throws IOException {
        final URL url = new URL(URL + core + "-platform");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");

        final int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            byte[] bytes;
            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = in.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                bytes = buffer.toByteArray();
                if (bytes.length == 0) {
                    throw new IllegalStateException("Empty stream");
                }
            }

            Files.write(path, bytes);
            return path;
        }

        throw new IllegalArgumentException("Connection responses a different code "+ responseCode);
    }

}
