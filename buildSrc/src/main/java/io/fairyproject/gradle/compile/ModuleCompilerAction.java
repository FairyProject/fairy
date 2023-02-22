package io.fairyproject.gradle.compile;

import com.google.common.collect.ImmutableList;
import io.fairyproject.gradle.file.ClassModifier;
import io.fairyproject.gradle.file.ClassModifierCancellable;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ModuleCompilerAction implements Action<Task> {

    private static final List<ClassModifier> MODIFIERS = ImmutableList.of(
            new ClassModifierCancellable()
    );

    @Override
    public void execute(@NotNull Task task) {
        try {
            doPostCompile((AbstractCompile) task);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void doPostCompile(AbstractCompile compile) throws IOException {
        for (File file : compile.getDestinationDirectory().getAsFileTree()) {
            if (!file.isFile() || !file.getName().endsWith(".class"))
                continue;

            boolean changed = false;
            byte[] bytes = Files.readAllBytes(file.toPath());
            ClassReader classReader = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();

            classReader.accept(
                    classNode,
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
            );

            for (ClassModifier modifier : MODIFIERS) {
                byte @Nullable [] modify = modifier.modify(classNode, classReader);
                if (modify != null) {
                    changed = true;
                    bytes = modify;
                    classReader = new ClassReader(bytes);
                    classNode = new ClassNode();

                    classReader.accept(
                            classNode,
                            ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
                    );
                }
            }

            if (changed)
                Files.write(file.toPath(), bytes);
        }
    }

}
