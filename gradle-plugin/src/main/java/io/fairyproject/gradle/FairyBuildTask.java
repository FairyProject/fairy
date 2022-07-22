package io.fairyproject.gradle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import io.fairyproject.gradle.file.*;
import io.fairyproject.gradle.util.ParallelThreadingUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

@Getter
@Setter
@CacheableTask
public class FairyBuildTask extends DefaultTask {

    private static final List<ClassModifier> MODIFIERS = ImmutableList.of(
            new ClassModifierEvent(),
            new ClassModifierCancellable()
    );

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    private File inJar;

    @Optional
    @Input
    private String classifier;

    @Input
    private FairyBuildData extension;

    @Input
    private List<String> dependModules;

    @Input
    private Multimap<String, String> exclusions;

    @Internal
    private String mainClass;

    @Internal
    private transient final ReentrantLock lock = new ReentrantLock();

    @Internal
    private String pluginClass;

    @Internal
    private String appClass;

    @Internal
    private File outputFileInternal;

    public FairyBuildTask() {
        super();
    }

    @OutputFile
    public File getOutputFile() {
        if (this.outputFileInternal == null) {
            int index = inJar.getName().lastIndexOf('.');
            String fileName = inJar.getName().substring(0, index);
            if (classifier != null && fileName.endsWith("-" + classifier)) {
                fileName = fileName.substring(0, fileName.length() - ("-" + classifier).length());
                final File dest = new File(inJar.getParentFile(), fileName + "-shadow" + inJar.getName().substring(index));
                inJar.renameTo(dest);
                inJar = dest;

                index = inJar.getName().lastIndexOf('.');
            }
            String name = fileName + (classifier == null ? "" : "-" + classifier) + inJar.getName().substring(index);
            this.outputFileInternal = new File(inJar.getParentFile(), name);
        }
        return this.outputFileInternal;
    }

    @TaskAction
    public void run() throws IOException {
        this.pluginClass = this.extension.getMainPackage().replace('.', '/') + "/fairy/plugin/Plugin";
        this.appClass = this.extension.getMainPackage().replace('.', '/') + "/fairy/app/Application";

        final File outJar = this.getOutputFile();

        try (JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(outJar)))) {
            try (JarFile jarFile = new JarFile(inJar)) {
                ParallelThreadingUtil.invokeAll(jarFile.entries(), t -> {
                    try {
                        this.readFile(t, jarFile, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            final boolean libraryMode = extension.isLibraryMode();
            if (mainClass == null && !libraryMode) {
                System.out.println("No main class found! Is there something wrong?");
            }

            for (PlatformType platformType : this.extension.getFairyPlatforms()) {
                final FileGenerator fileGenerator = platformType.createFileGenerator();
                if (fileGenerator == null)
                    continue;

                final Pair<String, byte[]> pair = fileGenerator.generate(this.getProject(), this.extension, this.mainClass, this.dependModules);
                if (pair != null) {
                    out.putNextEntry(new JarEntry(pair.getLeft()));
                    out.write(pair.getRight());
                }
            }

            final Pair<String, byte[]> pair = new FileGeneratorFairy().generate(this.getProject(), this.extension, this.mainClass, this.dependModules);
            if (pair != null) {
                out.putNextEntry(new JarEntry(pair.getLeft()));
                out.write(pair.getRight());
            }
        }
    }

    private void readFile(JarEntry jarEntry, JarFile jarFile, JarOutputStream out) throws IOException {
        if (shouldExclude(jarEntry)) {
            return;
        }
        byte[] bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry));

        if (jarEntry.getName().endsWith(".class")) {
            // Read class through ASM
            ClassReader classReader = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();

            classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            // is Main Plugin class
            if (classNode.superName != null && (classNode.superName.equals(this.pluginClass) || classNode.superName.equals(this.appClass))) {
                if (mainClass != null) {
                    throw new IllegalStateException("Multiple main class found! (Current: " + mainClass + ", Another: " + classNode.name + ")");
                }
                mainClass = classNode.name.replace('/', '.');
            }

            for (ClassModifier modifier : MODIFIERS) {
                byte[] modified = modifier.modify(classNode, classReader);
                if (modified != null) {
                    bytes = modified;
                    classReader = new ClassReader(modified);
                    classNode = new ClassNode();

                    classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
            }
        }

        this.lock.lock();
        try {
            out.putNextEntry(new JarEntry(jarEntry.getName()));
            out.write(bytes);
        } finally {
            this.lock.unlock();
        }
    }

    public boolean shouldExclude(JarEntry jarEntry) {
        if (jarEntry.getName().equals("module.json")) {
            return true;
        }

        if (jarEntry.getName().equals("plugin.yml")) {
            return true;
        }

        for (Map.Entry<String, String> entry : this.exclusions.entries()) {
            if (jarEntry.getName().startsWith(entry.getKey()) &&
                    !this.dependModules.contains(entry.getValue())) {
                return true;
            }
        }

        return false;
    }

}
