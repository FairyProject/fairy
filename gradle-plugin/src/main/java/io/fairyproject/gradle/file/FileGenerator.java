package io.fairyproject.gradle.file;

import io.fairyproject.gradle.FairyExtension;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface FileGenerator {

    @Nullable Pair<String, byte[]> generate(FairyExtension extension, @Nullable String mainClassPath, Map<String, String> otherModules);

}
