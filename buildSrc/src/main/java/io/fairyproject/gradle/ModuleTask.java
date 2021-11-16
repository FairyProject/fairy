package io.fairyproject.gradle;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

@Setter
@Getter
public class ModuleTask extends DefaultTask {

    @InputFile
    private File inJar;

    @Input
    private ModuleExtension extension;

    @Setter
    @Input
    private Set<String> modules;

    @TaskAction
    public void run() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JarOutputStream out = new JarOutputStream(outputStream)) {
            try (JarFile jarFile = new JarFile(inJar)) {
                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();

                    // Write file no matter what
                    out.putNextEntry(jarEntry);
                    out.write(ByteStreams.toByteArray(jarFile.getInputStream(jarEntry)));
                }
            }

            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("name", extension.getName().get());
            jsonObject.addProperty("classPath", extension.getClassPath().get());
            jsonObject.addProperty("abstraction", extension.getAbstraction().get());

            JsonArray array = new JsonArray();
            for (String depend : modules) {
                Project dependProject = getProject().project(ModulePlugin.MODULE_PREFIX + depend);
                array.add(dependProject.getExtensions().getByType(ModuleExtension.class).getName().get());
            }

            jsonObject.add("depends", array);

            out.putNextEntry(new JarEntry("module.json"));
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            out.write(gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
        }

        Files.write(this.inJar.toPath(), outputStream.toByteArray());
    }

}
