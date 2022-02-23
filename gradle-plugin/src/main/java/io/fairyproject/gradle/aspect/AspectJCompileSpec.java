package io.fairyproject.gradle.aspect;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.compile.DefaultJvmLanguageCompileSpec;


@Getter
@Setter
public class AspectJCompileSpec extends DefaultJvmLanguageCompileSpec {

    private FileCollection aspectJClasspath;

    AspectJCompileOptions aspectJCompileOptions;

    private FileCollection additionalInpath;
}
