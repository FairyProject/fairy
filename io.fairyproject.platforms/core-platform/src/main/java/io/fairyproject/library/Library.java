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

import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Getter
public class Library {
    public static final String IMANITY_LIB_PACKAGE = "org.imanity.framework.libs.";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String versionPackage;
    private final String name;
    private final byte[] checksum;
    private final LibraryRepository repository;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    public Library(String groupId, String artifactId, String version, String checksum) {
        this(groupId, artifactId, version, version, checksum);
    }

    public Library(String groupId, String artifactId, String version, String checksum, LibraryRepository repository) {
        this(groupId, artifactId, version, version, checksum, repository);
    }

    public Library(String groupId, String artifactId, String versionPackage, String version, String checksum) {
        this(groupId, artifactId, version, versionPackage, checksum, null);
    }

    public Library(String groupId, String artifactId, String versionPackage, String version, String checksum, LibraryRepository repository) {
        this.groupId = rewriteEscaping(groupId);
        this.artifactId = rewriteEscaping(artifactId);
        this.name = rewriteEscaping(artifactId);
        this.version = version;
        this.versionPackage = versionPackage == null ? version : versionPackage;
        if (checksum != null && !checksum.isEmpty()) {
            this.checksum = Base64.getDecoder().decode(checksum);
        } else {
            this.checksum = null;
        }
        this.repository = repository != null ? repository : LibraryRepository.MAVEN_CENTRAL;
    }

    public URL getUrl(LibraryRepository repository) throws MalformedURLException {
        String repo = repository.getUrl();
        if (!repo.endsWith("/")) {
            repo += "/";
        }
        repo += "%s/%s/%s/%s-%s.jar";

        String url = String.format(repo, this.groupId.replace(".", "/"), this.artifactId, this.versionPackage, this.artifactId, this.version);
        return new URL(url);
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String name() {
        return this.name;
    }

    public String getFileName() {
        return this.name.toLowerCase().replace('_', '-') + "-" + this.version;
    }

    public boolean checksumMatches(byte[] hash) {
        if (this.checksum == null) {
            return true;
        }
        return Arrays.equals(this.checksum, hash);
    }

    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static Library fromJsonObject(JsonObject jsonObject, String shadedPackage) {
        final String groupId;
        final String artifactId;
        final String version;
        if (jsonObject.has("dependency")) {
            final String dependency = jsonObject.get("dependency").getAsString();
            final String[] split = dependency.split(":");

            groupId = split[0];
            artifactId = split[1];
            version = split[2];
        } else {
            groupId = jsonObject.get("groupId").getAsString();
            artifactId = jsonObject.get("artifactId").getAsString();
            version = jsonObject.get("version").getAsString();
        }

        LibraryRepository libraryRepository = null;
        if (jsonObject.has("repository")) {
            final String repository = jsonObject.get("repository").getAsString();
            if (repository.equals("#mavenCentral")) {
                libraryRepository = LibraryRepository.MAVEN_CENTRAL;
            } else {
                libraryRepository = new LibraryRepository(repository);
            }
        }

        String checksum = null;
        if (jsonObject.has("checksum")) {
            checksum = jsonObject.get("checksum").getAsString();
        }

        return new Library(groupId, artifactId, null, version, checksum, libraryRepository);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return Objects.equals(version, library.version) && Objects.equals(name, library.name) && Arrays.equals(checksum, library.checksum) && Objects.equals(repository, library.repository);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(version, name, repository);
        result = 31 * result + Arrays.hashCode(checksum);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String groupId;
        private String artifactId;
        private String version;
        private String versionPackaging;
        private String checksum;
        private LibraryRepository repository = LibraryRepository.MAVEN_CENTRAL;

        public Builder gradle(@NotNull String dependency) {
            final String[] split = dependency.split(":");
            if (split.length != 3) {
                throw new IllegalArgumentException("Cannot parse gradle dependency tag.");
            }

            this.groupId(split[0]);
            this.artifactId(split[1]);
            this.version(split[2]);
            return this;
        }

        public Builder groupId(@NotNull String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder artifactId(@NotNull String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder version(@NotNull String version) {
            this.version = version;
            this.versionPackaging = version;
            return this;
        }

        public Builder version(@NotNull String version, @NotNull String versionPackaging) {
            this.version = version;
            this.versionPackaging = versionPackaging;
            return this;
        }

        public Builder repository(@Nullable LibraryRepository repository) {
            this.repository = repository;
            return this;
        }

        public Builder repository(@NotNull String repository) {
            this.repository = new LibraryRepository(repository);
            return this;
        }

        public Builder checksum(@NotNull String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Library build() {
            assert this.groupId != null;
            assert this.artifactId != null;
            assert this.version != null;
            assert this.versionPackaging != null;

            return new Library(this.groupId, this.artifactId, this.versionPackaging, this.version, checksum, this.repository);
        }

    }

}
