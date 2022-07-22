package io.fairyproject.gradle.file;

import io.fairyproject.gradle.FairyBuildData;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Project;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FileGenerator {

    @Nullable Pair<String, byte[]> generate(Project project, FairyBuildData extension, @Nullable String mainClassPath, List<String> otherModules);

}
