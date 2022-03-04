package io.fairyproject.bootstrap.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class DownloadUtil {

    private final String URL = "https://maven.imanity.dev/service/rest/v1/search/assets/download?sort=version&repository=imanity-libraries&maven.groupId=io.fairyproject&maven.artifactId=<module>";

    @SuppressWarnings("Duplicates")
    public Path download(Path path, String core) throws IOException {
        if (Boolean.getBoolean("fairy.project-ide")) {
            // Running in IDE
            final File projectFolder = Paths.get("").toAbsolutePath().getParent().getParent().toFile(); // double parent
            final File localRepoFolder = new File(projectFolder, "libs/local");

            File file = new File(localRepoFolder, "io/fairyproject/" + core + "-platform/latest/" + core + "-platform-latest.jar");
            if (!file.exists()) {
                throw new IllegalStateException("Couldn't found local module at local repo setup at " + file + "!");
            }
            return file.toPath();
        }

        final URL url = new URL(URL.replaceAll("<module>", core + "-platform"));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

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
