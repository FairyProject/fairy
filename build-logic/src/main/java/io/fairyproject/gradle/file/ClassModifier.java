package io.fairyproject.gradle.file;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public interface ClassModifier {

    @Nullable
    byte[] modify(ClassNode classNode, ClassReader classReader);

}
