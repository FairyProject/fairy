/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package io.fairyproject.library;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class LibraryRepository {

    public static final LibraryRepository MAVEN_CENTRAL = new LibraryRepository("https://repo1.maven.org/maven2/");

    private final String url;

    LibraryRepository(String url) {
        this.url = url;
    }

    protected URLConnection openConnection(Library library) throws IOException {
        URL url = new URL(this.url + library.getMavenRepoPath());
        return url.openConnection();
    }

    public byte[] downloadRaw(Library dependency) throws LibraryDownloadException {
        try {
            URLConnection connection = openConnection(dependency);
            try (InputStream in = connection.getInputStream()) {
                byte[] bytes = ByteStreams.toByteArray(in);
                if (bytes.length == 0) {
                    throw new LibraryDownloadException("Empty stream");
                }
                return bytes;
            }
        } catch (Exception e) {
            throw new LibraryDownloadException(e);
        }
    }

    public byte[] download(Library dependency) throws LibraryDownloadException {
        byte[] bytes = downloadRaw(dependency);

        // compute a hash for the downloaded file
        byte[] hash = Library.createDigest().digest(bytes);

        // ensure the hash matches the expected checksum
        if (!dependency.checksumMatches(hash)) {
            throw new LibraryDownloadException("Downloaded file had an invalid hash. " +
                    "Expected: " + Base64.getEncoder().encodeToString(dependency.getChecksum()) + " " +
                    "Actual: " + Base64.getEncoder().encodeToString(hash));
        }

        return bytes;
    }

    public void download(Library dependency, Path file) throws LibraryDownloadException {
        try {
            Files.write(file, download(dependency));
        } catch (IOException e) {
            throw new LibraryDownloadException(e);
        }
    }
}
