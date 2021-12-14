package io.fairyproject.gradle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FairyVersion implements Comparable<FairyVersion> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9*]+)\\.(?<minor>[0-9*]+)\\.(?<revision>[0-9*]+)b(?<build>[0-9*]+)");

    public static FairyVersion parse(String version) {
        final Matcher matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Incorrect version format: " + version);
        }
        return FairyVersion.builder()
                .major(Integer.parseInt(matcher.group("major")))
                .minor(Integer.parseInt(matcher.group("minor")))
                .revision(Integer.parseInt(matcher.group("revision")))
                .build(Integer.parseInt(matcher.group("build")))
                .build();
    }

    private int major;
    private int minor;
    private int revision;
    private int build;

    public boolean isAbove(FairyVersion version) {
        return this.compareTo(version) > 0;
    }

    public boolean isBelow(FairyVersion version) {
        return this.compareTo(version) < 0;
    }

    public boolean isOrAbove(FairyVersion version) {
        return this.compareTo(version) >= 0;
    }

    public boolean isOrBelow(FairyVersion version) {
        return this.compareTo(version) <= 0;
    }

    public void addMajor(int count) {
        this.major += count;
    }

    public void addMinor(int count) {
        this.minor += count;
    }

    public void addRevision(int count) {
        this.revision += count;
    }

    public void addBuild(int count) {
        this.build += count;
    }

    @Override
    public String toString() {
        return this.major + "." + minor + "." + revision + "b" + build;
    }

    @Override
    public int compareTo(@NotNull FairyVersion version) {
        if (this.equals(version)) {
            return 0;
        }

        if (this.major != version.major) {
            return this.major - version.major;
        }

        if (this.minor != version.minor) {
            return this.minor - version.minor;
        }

        if (this.revision != version.revision) {
            return this.revision - version.revision;
        }

        return this.build - version.revision;
    }
}
