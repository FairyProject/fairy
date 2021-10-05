package org.fairy.gradle.file;

import org.apache.commons.lang3.tuple.Pair;
import org.fairy.gradle.FairyExtension;

public interface FileGenerator {

    Pair<String, byte[]> generate(FairyExtension extension, String mainClassPath);

}
