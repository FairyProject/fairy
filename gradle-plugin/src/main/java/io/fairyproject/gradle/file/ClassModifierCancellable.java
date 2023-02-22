package io.fairyproject.gradle.file;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ClassModifierCancellable implements ClassModifier {

    @Override
    public @Nullable byte[] modify(ClassNode classNode, ClassReader classReader) {
        if (!classNode.interfaces.contains("io/fairyproject/event/Cancellable"))
            return null;

        boolean generateIsCancelled = !hasIsCancelledMethod(classNode);
        boolean generateSetCancelled = !hasSetCancelledMethod(classNode);
        boolean generateField = !hasCancelledField(classNode);

        if (!generateIsCancelled && !generateSetCancelled)
            return null;

        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classReader.accept(new CancellableVisitor(
                classNode,
                classWriter,
                generateField,
                generateIsCancelled,
                generateSetCancelled
        ), ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    public boolean hasIsCancelledMethod(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            final Type[] types = Type.getArgumentTypes(method.desc);
            if (types.length == 1 && Objects.equals(types[0], Type.BOOLEAN_TYPE)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSetCancelledMethod(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("setCancelled") && method.desc.equals("(Z)V")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCancelledField(ClassNode classNode) {
        for (FieldNode field : classNode.fields) {
            if (field.name.equals("cancelled") && Type.getType(field.desc) == Type.BOOLEAN_TYPE) {
                return true;
            }
        }
        return false;
    }

    private static class CancellableVisitor extends ClassVisitor {

        private final ClassNode classNode;
        private final ClassWriter classWriter;
        private final boolean generateField;
        private final boolean generateIsCancelled;
        private final boolean generateSetCancelled;

        public CancellableVisitor(
                ClassNode classNode,
                ClassWriter classWriter,
                boolean generateField,
                boolean generateIsCancelled,
                boolean generateSetCancelled) {
            super(ASM5, classWriter);

            this.classNode = classNode;
            this.classWriter = classWriter;
            this.generateField = generateField;
            this.generateIsCancelled = generateIsCancelled;
            this.generateSetCancelled = generateSetCancelled;
        }

        @Override
        public void visitEnd() {
            if (generateField)
                generateCancelledField();
            if (generateIsCancelled)
                generateIsCancelledMethod();
            if (generateSetCancelled)
                generateSetCancelledMethod();
        }

        private void generateSetCancelledMethod() {
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "setCancelled", "(Z)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(16, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, classNode.name, "cancelled", "Z");
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(17, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "L" + classNode.name + ";", null, label0, label2, 0);
            methodVisitor.visitLocalVariable("cancelled", "Z", null, label0, label2, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }

        private void generateIsCancelledMethod() {
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "isCancelled", "()Z", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(11, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, classNode.name, "cancelled", "Z");
            methodVisitor.visitInsn(IRETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + classNode.name + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }

        private void generateCancelledField() {
            FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE, "cancelled", "Z", null, null);
            fieldVisitor.visitEnd();
        }
    }
}
