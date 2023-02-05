/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.mc.protocol;

import io.fairyproject.util.exceptionally.ThrowingSupplier;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

@RequiredArgsConstructor
public class PacketEventsGithubResourceProvider implements Function<String, InputStream> {

    private final String url = "https://raw.githubusercontent.com/FairyProject/packetevents/2.0/api/src/main/resources/%s";
    private final Path cacheDirectory;

    @Override
    public InputStream apply(String s) {
        return ThrowingSupplier.sneaky(() -> {
            Path path = cacheDirectory.resolve(s);
            if (Files.exists(path))
                return Files.newInputStream(path);

            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(url, s)).openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] bytes = readAllBytes(inputStream);
                Files.createFile(path);
                Files.write(path, bytes, StandardOpenOption.WRITE);

                return new ByteArrayInputStream(bytes);
            }
        }).get();
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }
}
