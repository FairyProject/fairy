package org.fairy.gradle;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.fairy.gradle.file.FileGenerator;
import org.fairy.gradle.file.FileGeneratorFairy;
import org.fairy.gradle.relocator.JarRelocator;
import org.fairy.gradle.relocator.Relocation;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

@Getter
@Setter
public class FairyTask extends DefaultTask {

    private static final String PLUGIN_CLASS_PATH = "org/fairy/plugin/Plugin";

    @InputFile
    private File inJar;

    @Optional
    @Input
    private String classifier;

    @Input
    private List<Relocation> relocations;

    @Input
    private FairyExtension extension;

    @TaskAction
    public void run() throws IOException {
        int index = inJar.getName().lastIndexOf('.');
        String name = inJar.getName().substring(0, index) + (classifier == null ? "" : "-" + classifier) + inJar.getName().substring(index);
        File outJar = new File(inJar.getParentFile(), name);

        File tempOutJar = File.createTempFile(name, ".jar");
        Files.copy(inJar.toPath(), tempOutJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(tempOutJar))) {
            String mainClass = null;

            try (JarFile jarFile = new JarFile(inJar)) {
                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();

                    // Write file no matter what
                    out.putNextEntry(jarEntry);
                    out.write(ByteStreams.toByteArray(jarFile.getInputStream(jarEntry)));

                    if (jarEntry.getName().endsWith(".class")) {
                        // Read class through ASM
                        ClassReader classReader = new ClassReader(ByteStreams.toByteArray(jarFile.getInputStream(jarEntry)));
                        ClassNode classNode = new ClassNode();

                        classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                        // is Main Plugin class
                        if (classNode.superName != null && classNode.superName.equals(PLUGIN_CLASS_PATH)) {
                            if (mainClass != null) {
                                throw new IllegalStateException("Multiple main class found! (Current: " + mainClass + ", Another: " + classNode.name + ")");
                            }
                            mainClass = classNode.name.replace('/', '.');
                        }
                    }
                }
            }

            if (mainClass == null) {
                throw new IllegalStateException("No main class found!");
            }

            for (PlatformType platformType : this.extension.getFairyPlatforms().get()) {
                final FileGenerator fileGenerator = platformType.createFileGenerator();

                final Pair<String, byte[]> pair = fileGenerator.generate(this.extension, mainClass);
                out.putNextEntry(new JarEntry(pair.getLeft()));
                out.write(pair.getRight());
            }

            final Pair<String, byte[]> pair = new FileGeneratorFairy().generate(this.extension, mainClass);
            out.putNextEntry(new JarEntry(pair.getLeft()));
            out.write(pair.getRight());
        }

        JarRelocator jarRelocator = new JarRelocator(tempOutJar, outJar, this.relocations);
        jarRelocator.run();
    }

}
