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

package io.fairyproject.config.yaml;

import io.fairyproject.config.Comments;
import io.fairyproject.config.ConfigurationSource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

final class YamlSource implements ConfigurationSource<YamlConfiguration> {
    private static final int MAX_SYMBOLIC_LINKS = 40;

    private final Path configPath;
    private final YamlConfiguration.YamlProperties props;
    private final Yaml yaml;

    public YamlSource(Path configPath, YamlConfiguration.YamlProperties props) {
        this.configPath = Objects.requireNonNull(configPath);
        this.props = props;
        this.yaml = new Yaml(
                props.getConstructor(), props.getRepresenter(),
                props.getOptions(), props.getResolver()
        );
    }

    @Override
    public void saveConfiguration(YamlConfiguration config, Map<String, Object> map)
            throws IOException {
        createParentDirectories();
        CommentAdder adder = new CommentAdder(
                yaml.dump(map), config.getComments(), props
        );
        String commentedDump = adder.getCommentedDump();
        writeAtomically(commentedDump.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes to a temporary file next to the config and moves it over the config, so a crash
     * in the middle of a save leaves either the old or the new file, never a truncated one.
     * The file can't always be replaced (bind mounted file, directory that isn't writable, owner
     * that can't be kept), it is then written in place instead of failing the save.
     */
    private void writeAtomically(byte[] content) throws IOException {
        Path target = resolveSymbolicLinks(configPath.toAbsolutePath());
        Path temporary = target.resolveSibling(target.getFileName() + "." + UUID.randomUUID() + ".tmp");
        try {
            writeTemporary(temporary, target, content);
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (FileSystemException e) {
            Files.write(target, content);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * Moving over a symbolic link would replace the link itself and detach it from its target.
     * Not {@link Path#toRealPath}: the target may not exist yet.
     */
    private static Path resolveSymbolicLinks(Path path) throws IOException {
        Path current = path;
        for (int i = 0; i < MAX_SYMBOLIC_LINKS && Files.isSymbolicLink(current); i++) {
            current = current.resolveSibling(Files.readSymbolicLink(current));
        }
        return current;
    }

    private static void writeTemporary(Path temporary, Path target, byte[] content) throws IOException {
        // created with the permissions already, the content may be a password
        PosixFileAttributes attributes = readPosixAttributes(target);
        FileAttribute<?>[] creationAttributes = attributes == null
                ? new FileAttribute<?>[0]
                : new FileAttribute<?>[]{PosixFilePermissions.asFileAttribute(attributes.permissions())};

        try (FileChannel channel = FileChannel.open(temporary, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), creationAttributes)) {
            if (attributes != null) {
                copyPosixAttributes(attributes, temporary);
            } else {
                copyAcl(target, temporary);
            }

            ByteBuffer buffer = ByteBuffer.wrap(content);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
    }

    private static PosixFileAttributes readPosixAttributes(Path path) throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        return view != null && Files.exists(path) ? view.readAttributes() : null;
    }

    private static void copyPosixAttributes(PosixFileAttributes attributes, Path path) throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        PosixFileAttributes current = view.readAttributes();
        if (!attributes.owner().equals(current.owner())) {
            view.setOwner(attributes.owner());
        }
        if (!attributes.group().equals(current.group())) {
            view.setGroup(attributes.group());
        }
        // the umask may have narrowed the permissions given on creation
        view.setPermissions(attributes.permissions());
    }

    private static void copyAcl(Path source, Path destination) throws IOException {
        AclFileAttributeView sourceView = Files.getFileAttributeView(source, AclFileAttributeView.class);
        AclFileAttributeView destinationView = Files.getFileAttributeView(destination, AclFileAttributeView.class);
        if (sourceView != null && destinationView != null && Files.exists(source)) {
            destinationView.setAcl(sourceView.getAcl());
        }
    }

    private void createParentDirectories() throws IOException {
        Path parentDir = configPath.toAbsolutePath().getParent();
        if (!Files.isDirectory(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }

    @Override
    public Map<String, Object> loadConfiguration(YamlConfiguration config)
            throws IOException {
        String cfg = readConfig();
        return (Map<String, Object>) yaml.load(cfg);
    }

    private String readConfig() throws IOException {
        // Files.lines keeps the file open until the stream is closed
        try (Stream<String> lines = Files.lines(configPath)) {
            return lines.collect(joining("\n"));
        }
    }

    private static final class CommentAdder {
        private static final Pattern PREFIX_PATTERN = Pattern.compile("(^\\w+):(.*)");
        private static final Pattern SPACES_PATTERN = Pattern.compile("^[ \\s\\n]+.*");
        private final String dump;
        private final Comments comments;
        private final YamlComments yamlComments;
        private final YamlConfiguration.YamlProperties props;
        private final StringBuilder builder;

        private CommentAdder(String dump, Comments comments,
                             YamlConfiguration.YamlProperties props
        ) {
            this.dump = dump;
            this.props = props;
            this.comments = comments;
            this.yamlComments = new YamlComments(comments);
            this.builder = new StringBuilder(dump.length());
        }

        public String getCommentedDump() {
            addComments(props.getPrependedComments());
            addClassComments();
            addFieldComments();
            addComments(props.getAppendedComments());
            return builder.toString();
        }

        private void addComments(List<String> comments) {
            for (String comment : comments) {
                if (!comment.isEmpty()) {
                    builder.append("# ").append(comment);
                }
                builder.append('\n');
            }
        }

        private void addClassComments() {
            if (comments.hasClassComments()) {
                builder.append(yamlComments.classCommentsAsString());
                builder.append("\n");
            }
        }

        private void addFieldComments() {
            if (comments.hasFieldComments()) {
                List<String> dumpLines = Arrays.asList(dump.split("\n"));
                addDumpLines(dumpLines);
            } else {
                builder.append(dump);
            }
        }

        private void addDumpLines(List<String> dumpLines) {
            for (String dumpLine : dumpLines) {
                Matcher m = PREFIX_PATTERN.matcher(this.trimStart(dumpLine));
                if (m.matches()) {
                    addFieldComment(dumpLine, this.trimStart(dumpLine), m);
                }
                builder.append(dumpLine).append('\n');
            }
        }

        private StringBuilder data = new StringBuilder();

        private void addFieldComment(String dumpLine, String dumpLineWithTrim, Matcher matcher) {
            Map<String, String> map = yamlComments.fieldCommentAsStrings(props.getFormatter());
            Matcher spacesMatch = SPACES_PATTERN.matcher(dumpLine);
            String value = matcher.group(2);
            if (value.equals("") || value.isEmpty()) {
                if (!spacesMatch.matches()) {
                    data = new StringBuilder();
                }
                data.append(matcher.group(1)).append(".");
            } else {
                if (!spacesMatch.matches()) {
                    data = new StringBuilder();
                }
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String prefix = entry.getKey() + ":";
                String lineData = data.toString().replaceFirst("[.]$", "");
                String[] splitPrefix = prefix.split("\\.");
                if (dumpLine.startsWith(prefix) || (lineData.startsWith(splitPrefix[0]) && dumpLineWithTrim.startsWith(splitPrefix[splitPrefix.length - 1]))) {
                    String newDumpLine = dumpLine.replaceAll("^\\s+", "");
                    int count = dumpLine.length() - newDumpLine.length();
                    String[] valueSplit = entry.getValue().split("\n");
                    for (String val : valueSplit) {
                        int updateCount = val.length() + count;
                        char fill = ' ';
                        builder.append(new String(new char[updateCount - val.length()]).replace('\0', fill)).append(val).append("\n");
                    }
                    break;
                }
            }

        }

        private String trimStart(String value) {
            return value.replaceFirst("^\\s+", "");
        }
    }
}
