package io.fairyproject.gradle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import io.fairyproject.gradle.file.*;
import io.fairyproject.gradle.relocator.JarRelocator;
import io.fairyproject.gradle.relocator.Relocation;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

@Getter
@Setter
@CacheableTask
public class FairyTask extends DefaultTask {

    private static final String PLUGIN_CLASS_PATH = "io/fairyproject/plugin/Plugin";
    private static final String APPLICATION_CLASS_PATH = "io/fairyproject/app/Application";
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
    private List<Relocation> relocations;

    @Input
    private FairyExtension extension;

    @Input
    private Map<String, String> dependModules;

    @Input
    private Multimap<String, String> exclusions;

    @TaskAction
    public void run() throws IOException {
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
        File outJar = new File(inJar.getParentFile(), name);

        File tempOutJar = File.createTempFile(name, ".jar");
        String mainClass = null;

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(tempOutJar))) {
            try (JarFile jarFile = new JarFile(inJar)) {
                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();

                    // Write file no matter what
                    out.putNextEntry(new JarEntry(jarEntry.getName()));
                    byte[] bytes = null;

                    if (jarEntry.getName().endsWith(".class")) {
                        // Read class through ASM
                        ClassReader classReader = new ClassReader(ByteStreams.toByteArray(jarFile.getInputStream(jarEntry)));
                        ClassNode classNode = new ClassNode();

                        classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                        // is Main Plugin class
                        if (classNode.superName != null && (classNode.superName.equals(PLUGIN_CLASS_PATH) || classNode.superName.equals(APPLICATION_CLASS_PATH))) {
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

                    if (bytes == null) {
                        bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry));
                    }
                    out.write(bytes);
                }
            }
        }

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(outJar))) {
            final Boolean libraryMode = extension.getLibraryMode().get();
            if (mainClass == null && !libraryMode) {
                System.out.println("No main class found! Is there something wrong?");
            }

            if (!libraryMode) {
                JarRelocator jarRelocator = new JarRelocator(tempOutJar, outJar, this.relocations, ImmutableSet.of());
                jarRelocator.run();
            } else {
                Files.write(outJar.toPath(), Files.readAllBytes(tempOutJar.toPath()));
            }

            for (PlatformType platformType : this.extension.getFairyPlatforms().get()) {
                final FileGenerator fileGenerator = platformType.createFileGenerator();
                if (fileGenerator == null)
                    continue;

                final Pair<String, byte[]> pair = fileGenerator.generate(this.extension, mainClass, this.dependModules);
                if (pair != null) {
                    out.putNextEntry(new JarEntry(pair.getLeft()));
                    out.write(pair.getRight());
                }
            }

            final Pair<String, byte[]> pair = new FileGeneratorFairy().generate(this.extension, mainClass, this.dependModules);
            if (pair != null) {
                out.putNextEntry(new JarEntry(pair.getLeft()));
                out.write(pair.getRight());
            }
        }
    }

}
