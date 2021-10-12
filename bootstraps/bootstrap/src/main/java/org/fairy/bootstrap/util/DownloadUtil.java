package org.fairy.bootstrap.util;

import com.google.common.io.ByteStreams;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class DownloadUtil {

    private final String URL = "https://maven.imanity.dev/service/rest/v1/search/assets/download?sort=version&repository=imanity-libraries&maven.groupId=org.fairy&maven.artifactId=";

    public Path download(Path path, String core) throws IOException {
        final URL url = new URL(URL + core + "-platform");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        byte[] bytes;
        try (InputStream in = connection.getInputStream()) {
            bytes = ByteStreams.toByteArray(in);
            if (bytes.length == 0) {
                throw new IllegalStateException("Empty stream");
            }
        }

        Files.write(path, bytes);
        return path;
    }

}
