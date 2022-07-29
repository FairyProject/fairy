package io.fairyproject.gradle;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.file.ClassModifier;
import io.fairyproject.gradle.file.ClassModifierCancellable;
import io.fairyproject.gradle.file.ClassModifierEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

@Setter
@Getter
public class ModuleTask extends DefaultTask {

    private static final List<ClassModifier> MODIFIERS = ImmutableList.of(
            new ClassModifierEvent(),
            new ClassModifierCancellable()
    );

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    private File inJar;

    @Input
    private ModuleExtensionSerializable extension;

    @Input
    private List<Pair<String, String>> exclusives;

    @Input
    private boolean snapshot;

    @TaskAction
    public void run() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JarOutputStream out = new JarOutputStream(outputStream)) {
            try (JarFile jarFile = new JarFile(inJar)) {
                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();

                    if (jarEntry.getName().equalsIgnoreCase("module.json"))
                        continue;

                    // Write file no matter what
                    out.putNextEntry(new JarEntry(jarEntry.getName()));

                    byte[] bytes = null;
                    if (jarEntry.getName().endsWith(".class")) {
                        // Read class through ASM
                        ClassReader classReader = new ClassReader(ByteStreams.toByteArray(jarFile.getInputStream(jarEntry)));
                        ClassNode classNode = new ClassNode();

                        classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

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

            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("name", extension.getName());
            jsonObject.addProperty("classPath", extension.getClassPath());
            jsonObject.addProperty("abstraction", extension.isAbstraction());

            JsonObject exclusive = new JsonObject();
            final Map<String, String> map = extension.getExclusives();
            map.forEach(exclusive::addProperty);
            exclusives.forEach(pair -> exclusive.addProperty(pair.getKey(), pair.getValue()));

            jsonObject.add("exclusive", exclusive);

            JsonArray array = new JsonArray();
            for (Lib library : extension.getLibraries()) {
                array.add(library.toJsonObject());
            }
            jsonObject.add("libraries", array);

            out.putNextEntry(new JarEntry("module.json"));
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            out.write(gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
        }

        Files.write(this.inJar.toPath(), outputStream.toByteArray());
    }

}
