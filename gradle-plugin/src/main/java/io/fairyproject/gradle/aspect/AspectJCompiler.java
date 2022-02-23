package io.fairyproject.gradle.aspect;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecHandle;
import org.gradle.process.internal.JavaExecHandleBuilder;
import org.gradle.process.internal.JavaExecHandleFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AspectJCompiler implements Compiler<AspectJCompileSpec> {

    private final JavaExecHandleFactory javaExecHandleFactory;

    @Override
    public WorkResult execute(AspectJCompileSpec spec) {

        ExecHandle handle = createCompilerHandle(spec);
        executeCompiler(handle);

        return WorkResults.didWork(true);
    }

    @SneakyThrows
    private ExecHandle createCompilerHandle(AspectJCompileSpec spec) {
        JavaExecHandleBuilder ajc = javaExecHandleFactory.newJavaExec();
        ajc.setWorkingDir(spec.getWorkingDir());
        ajc.setClasspath(spec.getAspectJClasspath());
        ajc.getMainClass().set("org.aspectj.tools.ajc.Main");

        AjcForkOptions forkOptions = spec.getAspectJCompileOptions().getForkOptions();

        ajc.setMinHeapSize(forkOptions.getMemoryInitialSize());
        ajc.setMaxHeapSize(forkOptions.getMemoryMaximumSize());
        ajc.jvmArgs(forkOptions.getJvmArgs());

        List<String> args = new LinkedList<>();

        Collection<File> inpath = new LinkedHashSet<>();

        if (spec.getAdditionalInpath() != null && !spec.getAdditionalInpath().isEmpty()) {
            inpath.addAll(spec.getAdditionalInpath().getFiles());
        }

        if (!spec.getAspectJCompileOptions().getInpath().isEmpty()) {
            inpath.addAll(spec.getAspectJCompileOptions().getInpath().getFiles());
        }

        this.addCompileArgs(args, inpath, spec);

        spec.getAspectJCompileOptions().getCompilerArgumentProviders()
                .forEach(commandLineArgumentProvider -> commandLineArgumentProvider.asArguments().forEach(args::add));

        if (spec.getSourceFiles() != null) {
            spec.getSourceFiles().forEach(sourceFile ->
                    args.add(sourceFile.getAbsolutePath())
            );
        }

        File argFile = new File(spec.getTempDir(), "ajc.options");

        Files.write(argFile.toPath(), args, StandardCharsets.UTF_8);

        ajc.args("-argfile", argFile.getAbsolutePath());

        ajc.setIgnoreExitValue(true);
        return ajc.build();
    }

    private void addCompileArgs(List<String> args, Collection<File> inpath, AspectJCompileSpec spec) {
        this.addOptions(args, !inpath.isEmpty(), "-inpath", () -> getAsPath(inpath));

        final AspectJCompileOptions aspectJCompileOptions = spec.getAspectJCompileOptions();
        this.addOptions(args, !aspectJCompileOptions.getAspectpath().isEmpty(), "-aspectpath", () -> getAsPath(aspectJCompileOptions.getAspectpath().getFiles()));
        this.addOptions(args, aspectJCompileOptions.getOutjar().isPresent(), "-outjar", () -> aspectJCompileOptions.getOutjar().get().getAsFile().getAbsolutePath());
        this.addOptions(args, aspectJCompileOptions.getOutxml().getOrElse(false), "-outxml", null);
        this.addOptions(args, aspectJCompileOptions.getOutxmlfile().isPresent(), "-outxmlfile", () -> aspectJCompileOptions.getOutxmlfile().getOrNull());
        this.addOptions(args, !aspectJCompileOptions.getSourceroots().isEmpty(), "-sourceroots", () -> aspectJCompileOptions.getSourceroots().getAsPath());
        this.addOptions(args, aspectJCompileOptions.getCrossrefs().getOrElse(false), "-crossrefs", null);
        this.addOptions(args, !spec.getCompileClasspath().isEmpty(), "-classpath", () -> getAsPath(spec.getCompileClasspath()));
        this.addOptions(args, !aspectJCompileOptions.getBootclasspath().isEmpty(), "-bootclasspath", () -> getAsPath(aspectJCompileOptions.getBootclasspath().getFiles()));
        this.addOptions(args, !aspectJCompileOptions.getExtdirs().isEmpty(), "-extdirs", () -> getAsPath(aspectJCompileOptions.getExtdirs().getFiles()));
        this.addOptions(args, true, "-d", () -> spec.getDestinationDir().getAbsolutePath());
        this.addOptions(args, spec.getTargetCompatibility() != null, "-target", spec::getTargetCompatibility);
        this.addOptions(args, spec.getSourceCompatibility() != null, "-source", spec::getSourceCompatibility);
        this.addOptions(args, aspectJCompileOptions.getEncoding().isPresent(), "-encoding", () -> aspectJCompileOptions.getEncoding().get());
        this.addOptions(args, aspectJCompileOptions.getVerbose().getOrElse(false), "-verbose", null);

        args.addAll(aspectJCompileOptions.getCompilerArgs());
    }

    private void addOptions(List<String> args, boolean condition, String key, Supplier<Object> value) {
        if (condition) {
            args.add(key);
            if (value != null) {
                args.add(value.get().toString());
            }
        }
    }

    private void executeCompiler(ExecHandle handle) {
        handle.start();
        ExecResult result = handle.waitForFinish();
        if (result.getExitValue() != 0) {
            throw new CompilationFailedException(result.getExitValue());
        }
    }

    private String getAsPath(Collection<File> files) {
        return files.stream()
                .filter(File::exists)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(File.pathSeparator));
    }
}
