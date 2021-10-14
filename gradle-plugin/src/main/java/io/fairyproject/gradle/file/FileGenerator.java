package io.fairyproject.gradle.file;

import org.apache.commons.lang3.tuple.Pair;
import io.fairyproject.gradle.FairyExtension;

public interface FileGenerator {

    Pair<String, byte[]> generate(FairyExtension extension, String mainClassPath);

}
