package io.fairyproject.gradle.file;

import org.apache.commons.lang3.tuple.Pair;
import io.fairyproject.gradle.FairyExtension;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface FileGenerator {

    @Nullable Pair<String, byte[]> generate(FairyExtension extension, @Nullable String mainClassPath, Map<String, String> otherModules);

}
