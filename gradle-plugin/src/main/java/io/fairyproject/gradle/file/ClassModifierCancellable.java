package io.fairyproject.gradle.file;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import static org.objectweb.asm.Opcodes.*;

public class ClassModifierCancellable implements ClassModifier {
    @Override
    public @Nullable byte[] modify(ClassNode classNode, ClassReader classReader) {
        if (classNode.interfaces.contains("io/fairyproject/event/Cancellable")) {
            boolean generateIsCancelled = true;
            boolean generateSetCancelled = true;
            for (MethodNode method : classNode.methods) {
                switch (method.name) {
                    case "isCancelled":
                        generateIsCancelled = false;
                        break;
                    case "setCancelled":
                        final Type[] types = Type.getArgumentTypes(method.desc);
                        if (types.length == 1 && types[0] == Type.BOOLEAN_TYPE) {
                            generateSetCancelled = false;
                        }
                        break;
                }
            }

            if (!generateIsCancelled && !generateSetCancelled) {
                return null;
            }

            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            boolean finalGenerateIsCancelled = generateIsCancelled;
            boolean finalGenerateSetCancelled = generateSetCancelled;
            class CancellableVisitor extends ClassVisitor {
                public CancellableVisitor(ClassVisitor classVisitor) {
                    super(Opcodes.ASM5, classVisitor);
                }

                @Override
                public void visitEnd() {
                    if (finalGenerateIsCancelled) {
                        {
                            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "isCancelled", "()Z", null, null);
                            methodVisitor.visitCode();
                            Label label0 = new Label();
                            methodVisitor.visitLabel(label0);
                            methodVisitor.visitLineNumber(11, label0);
                            methodVisitor.visitVarInsn(ALOAD, 0);
                            methodVisitor.visitFieldInsn(GETFIELD, "io/fairyproject/event/impl/TestEvent", "cancelled", "Z");
                            methodVisitor.visitInsn(IRETURN);
                            Label label1 = new Label();
                            methodVisitor.visitLabel(label1);
                            methodVisitor.visitLocalVariable("this", "Lio/fairyproject/event/impl/TestEvent;", null, label0, label1, 0);
                            methodVisitor.visitMaxs(1, 1);
                            methodVisitor.visitEnd();
                        }
                    }

                    if (finalGenerateSetCancelled) {
                        {
                            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "setCancelled", "(Z)V", null, null);
                            methodVisitor.visitCode();
                            Label label0 = new Label();
                            methodVisitor.visitLabel(label0);
                            methodVisitor.visitLineNumber(16, label0);
                            methodVisitor.visitVarInsn(ALOAD, 0);
                            methodVisitor.visitVarInsn(ILOAD, 1);
                            methodVisitor.visitFieldInsn(PUTFIELD, "io/fairyproject/event/impl/TestEvent", "cancelled", "Z");
                            Label label1 = new Label();
                            methodVisitor.visitLabel(label1);
                            methodVisitor.visitLineNumber(17, label1);
                            methodVisitor.visitInsn(RETURN);
                            Label label2 = new Label();
                            methodVisitor.visitLabel(label2);
                            methodVisitor.visitLocalVariable("this", "Lio/fairyproject/event/impl/TestEvent;", null, label0, label2, 0);
                            methodVisitor.visitLocalVariable("cancelled", "Z", null, label0, label2, 1);
                            methodVisitor.visitMaxs(2, 2);
                            methodVisitor.visitEnd();
                        }
                    }
                }
            }

            classReader.accept(new CancellableVisitor(classWriter), ClassReader.EXPAND_FRAMES);
            return classWriter.toByteArray();
        }
        return null;
    }
}
