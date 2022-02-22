package io.fairyproject.gradle.aspect;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;


/**
 * @author Lars Grefer
 */
public class DefaultAspectjSourceDirectorySet extends DefaultSourceDirectorySet implements AspectjSourceDirectorySet {
    public DefaultAspectjSourceDirectorySet(SourceDirectorySet sourceSet) {
        super(sourceSet);
    }
}
