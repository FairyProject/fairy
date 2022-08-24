package io.fairyproject.gradle;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
public class ModuleResourceAction implements Action<Task> {

    @Input
    private ModuleExtensionSerializable extension;

    @Input
    private List<Pair<String, String>> exclusives;

    @Input
    private boolean snapshot;

    @Override
    public void execute(@NotNull Task task) {
        try {
            doPostJar((Jar) task);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void doPostJar(Jar task) throws Exception {
        File inJar = task.getArchiveFile().get().getAsFile();

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
                    out.write(ByteStreams.toByteArray(jarFile.getInputStream(jarEntry)));
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

        Files.write(inJar.toPath(), outputStream.toByteArray());
    }

}
