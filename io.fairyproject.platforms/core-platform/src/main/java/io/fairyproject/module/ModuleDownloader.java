package io.fairyproject.module;

import com.google.common.io.ByteStreams;
import io.fairyproject.Debug;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class ModuleDownloader {

    private final String URL = "https://maven.imanity.dev/service/rest/v1/search/assets/download?repository=imanity-libraries&maven.groupId=io.fairyproject&maven.artifactId=<module>&version=<version>&maven.extension=jar";


    @SuppressWarnings("Duplicates")
    public Path download(Path path, String module, String version) throws IOException {
        if (Files.exists(path)) {
            return path;
        }
        path.toFile().getParentFile().mkdirs();
        if (Debug.IN_FAIRY_IDE) {
            final File projectFolder = Paths.get("").toAbsolutePath().getParent().getParent().toFile(); // double parent
            final File localRepoFolder = new File(projectFolder, "libs/local");

            if (!localRepoFolder.exists()) {
                Debug.logExceptionAndPause(new IllegalStateException("Couldn't found local repo setup at " + localRepoFolder + "!"));
            }

            File file = new File(localRepoFolder, module + ".jar");
            if (!file.exists()) {
                Debug.logExceptionAndPause(new IllegalStateException("Couldn't found local module at local repo setup at " + file + "!"));
            }
            return file.toPath();
        }

        final java.net.URL url = new URL(URL.replaceAll("<module>", module).replaceAll("<version>", version)); // TODO - ensure url correctly
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");

        final int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
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

        throw new IllegalArgumentException("Connection responses a different code " + responseCode);
    }

}
