package io.fairyproject.gradle;

import com.google.common.io.ByteStreams;
import io.fairyproject.gradle.file.FileGenerator;
import io.fairyproject.gradle.file.FileGeneratorFairy;
import io.fairyproject.gradle.relocator.JarRelocator;
import io.fairyproject.gradle.relocator.Relocation;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.org.bouncycastle.util.Arrays;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static org.objectweb.asm.Opcodes.*;

@Getter
@Setter
public class FairyTask extends DefaultTask {

    private static final String PLUGIN_CLASS_PATH = "io/fairyproject/plugin/Plugin";
    private static final String APPLICATION_CLASS_PATH = "io/fairyproject/app/Application";

    @InputFile
    private File inJar;

    @Input
    private Set<File> relocateEntries;

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
        String fileName = inJar.getName().substring(0, index);
        if (fileName.endsWith("-" + classifier)) {
            fileName = fileName.substring(0, fileName.length() - ("-" + classifier).length());
            final File dest = new File(fileName + "-shadow" + inJar.getName().substring(index));
            inJar.renameTo(dest);
            inJar = dest;
        }
        String name = fileName + (classifier == null ? "" : "-" + classifier) + inJar.getName().substring(index);
        File outJar = new File(inJar.getParentFile(), name);

        File tempOutJar = File.createTempFile(name, ".jar");

        JarRelocator jarRelocator = new JarRelocator(inJar, tempOutJar, this.relocations, this.relocateEntries);
        jarRelocator.run();

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(outJar))) {
            String mainClass = null;

            try (JarFile jarFile = new JarFile(tempOutJar)) {
                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();

                    // Write file no matter what
                    out.putNextEntry(jarEntry);
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

                        if (classNode.visibleAnnotations != null) {
                            check: for (AnnotationNode visibleAnnotation : classNode.visibleAnnotations) {
                                // Generating Subscribers instance for Event classes
                                if (visibleAnnotation.desc != null && visibleAnnotation.desc.equals("Lio/fairyproject/event/Event;")) {
                                    for (FieldNode field : classNode.fields) {
                                        if (field.name.equals("SUBSCRIBERS")) {
                                            // Already has SUBSCRIBERS field
                                            if (!field.desc.equals("Lio/fairyproject/event/Subscribers;")) {
                                                throw new IllegalArgumentException("Class " + classNode.name + " contains field SUBSCRIBERS but not with type io/fairyproject/event/Subscribers");
                                            }
                                            if (!Modifier.isStatic(field.access)) {
                                                throw new IllegalArgumentException("Class " + classNode.name + " contains field SUBSCRIBERS but not static");
                                            }
                                            break check;
                                        }
                                    }

                                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

                                    class SubscribersVisitor extends ClassVisitor {

                                        public SubscribersVisitor(ClassVisitor classVisitor) {
                                            super(Opcodes.ASM5, classVisitor);
                                        }

                                        @Override
                                        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                                            if (!ArrayUtils.contains(interfaces, "io/fairyproject/event/IEvent")) {
                                                /*
                                                implements IEvent
                                                 */
                                                interfaces = ArrayUtils.add(interfaces, "io/fairyproject/event/IEvent");
                                            }
                                            super.visit(version, access, name, signature, superName, interfaces);
                                        }

                                        @Override
                                        public void visitEnd() {
                                            {
                                                /*
                                                    public static final Subscribers SUBSCRIBERS
                                                 */
                                                FieldVisitor fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "SUBSCRIBERS", "Lio/fairyproject/event/Subscribers;", null, null);
                                                fieldVisitor.visitEnd();
                                            }

                                            {
                                                /*
                                                    Subscribers child = null;
                                                    for (Class<?> type = Event.class.getSuperclass(); type != null; type = type.getSuperclass()) {
                                                        try {
                                                            final Field subscribers = type.getDeclaredField("SUBSCRIBERS");
                                                            subscribers.setAccessible(true);

                                                            child = (Subscribers) subscribers.get(null);
                                                            break;
                                                        } catch (NoSuchFieldException ignored) {
                                                        } catch (IllegalAccessException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    SUBSCRIBERS = new Subscribers(Event.class, child);
                                                 */
                                                MethodVisitor methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                                                methodVisitor.visitCode();
                                                Label label0 = new Label();
                                                Label label1 = new Label();
                                                Label label2 = new Label();
                                                methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/NoSuchFieldException");
                                                Label label3 = new Label();
                                                methodVisitor.visitTryCatchBlock(label0, label1, label3, "java/lang/IllegalAccessException");
                                                Label label4 = new Label();
                                                methodVisitor.visitLabel(label4);
                                                methodVisitor.visitLineNumber(13, label4);
                                                methodVisitor.visitInsn(ACONST_NULL);
                                                methodVisitor.visitVarInsn(ASTORE, 0);
                                                Label label5 = new Label();
                                                methodVisitor.visitLabel(label5);
                                                methodVisitor.visitLineNumber(14, label5);
                                                methodVisitor.visitLdcInsn(Type.getType("L" + classNode.name + ";"));
                                                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false);
                                                methodVisitor.visitVarInsn(ASTORE, 1);
                                                Label label6 = new Label();
                                                methodVisitor.visitLabel(label6);
                                                methodVisitor.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"io/fairyproject/event/Subscribers", "java/lang/Class"}, 0, null);
                                                methodVisitor.visitVarInsn(ALOAD, 1);
                                                Label label7 = new Label();
                                                methodVisitor.visitJumpInsn(IFNULL, label7);
                                                methodVisitor.visitLabel(label0);
                                                methodVisitor.visitLineNumber(16, label0);
                                                methodVisitor.visitVarInsn(ALOAD, 1);
                                                methodVisitor.visitLdcInsn("SUBSCRIBERS");
                                                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
                                                methodVisitor.visitVarInsn(ASTORE, 2);
                                                Label label8 = new Label();
                                                methodVisitor.visitLabel(label8);
                                                methodVisitor.visitLineNumber(17, label8);
                                                methodVisitor.visitVarInsn(ALOAD, 2);
                                                methodVisitor.visitInsn(ICONST_1);
                                                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
                                                Label label9 = new Label();
                                                methodVisitor.visitLabel(label9);
                                                methodVisitor.visitLineNumber(19, label9);
                                                methodVisitor.visitVarInsn(ALOAD, 2);
                                                methodVisitor.visitInsn(ACONST_NULL);
                                                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                                                methodVisitor.visitTypeInsn(CHECKCAST, "io/fairyproject/event/Subscribers");
                                                methodVisitor.visitVarInsn(ASTORE, 0);
                                                methodVisitor.visitLabel(label1);
                                                methodVisitor.visitLineNumber(20, label1);
                                                methodVisitor.visitJumpInsn(GOTO, label7);
                                                methodVisitor.visitLabel(label2);
                                                methodVisitor.visitLineNumber(21, label2);
                                                methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/NoSuchFieldException"});
                                                methodVisitor.visitVarInsn(ASTORE, 2);
                                                Label label10 = new Label();
                                                methodVisitor.visitLabel(label10);
                                                methodVisitor.visitLineNumber(24, label10);
                                                Label label11 = new Label();
                                                methodVisitor.visitJumpInsn(GOTO, label11);
                                                methodVisitor.visitLabel(label3);
                                                methodVisitor.visitLineNumber(22, label3);
                                                methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/IllegalAccessException"});
                                                methodVisitor.visitVarInsn(ASTORE, 2);
                                                Label label12 = new Label();
                                                methodVisitor.visitLabel(label12);
                                                methodVisitor.visitLineNumber(23, label12);
                                                methodVisitor.visitVarInsn(ALOAD, 2);
                                                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/IllegalAccessException", "printStackTrace", "()V", false);
                                                methodVisitor.visitLabel(label11);
                                                methodVisitor.visitLineNumber(14, label11);
                                                methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                                                methodVisitor.visitVarInsn(ALOAD, 1);
                                                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false);
                                                methodVisitor.visitVarInsn(ASTORE, 1);
                                                methodVisitor.visitJumpInsn(GOTO, label6);
                                                methodVisitor.visitLabel(label7);
                                                methodVisitor.visitLineNumber(26, label7);
                                                methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
                                                methodVisitor.visitVarInsn(ALOAD, 0);
                                                Label label13 = new Label();
                                                methodVisitor.visitJumpInsn(IFNONNULL, label13);
                                                Label label14 = new Label();
                                                methodVisitor.visitLabel(label14);
                                                methodVisitor.visitLineNumber(27, label14);
                                                methodVisitor.visitMethodInsn(INVOKESTATIC, "io/fairyproject/event/EventBus", "getGlobalSubscribers", "()Lio/fairyproject/event/Subscribers;", false);
                                                methodVisitor.visitVarInsn(ASTORE, 0);
                                                methodVisitor.visitLabel(label13);
                                                methodVisitor.visitLineNumber(30, label13);
                                                methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                                                methodVisitor.visitTypeInsn(NEW, "io/fairyproject/event/Subscribers");
                                                methodVisitor.visitInsn(DUP);
                                                methodVisitor.visitLdcInsn(Type.getType("L" + classNode.name + ";"));
                                                methodVisitor.visitVarInsn(ALOAD, 0);
                                                methodVisitor.visitMethodInsn(INVOKESPECIAL, "io/fairyproject/event/Subscribers", "<init>", "(Ljava/lang/Class;Lio/fairyproject/event/Subscribers;)V", false);
                                                methodVisitor.visitFieldInsn(PUTSTATIC, classNode.name, "SUBSCRIBERS", "Lio/fairyproject/event/Subscribers;");
                                                Label label15 = new Label();
                                                methodVisitor.visitLabel(label15);
                                                methodVisitor.visitLineNumber(31, label15);
                                                methodVisitor.visitInsn(RETURN);
                                                methodVisitor.visitLocalVariable("subscribers", "Ljava/lang/reflect/Field;", null, label8, label2, 2);
                                                methodVisitor.visitLocalVariable("e", "Ljava/lang/IllegalAccessException;", null, label12, label11, 2);
                                                methodVisitor.visitLocalVariable("type", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", label6, label7, 1);
                                                methodVisitor.visitLocalVariable("child", "Lio/fairyproject/event/Subscribers;", null, label5, label15, 0);
                                                methodVisitor.visitMaxs(4, 3);
                                                methodVisitor.visitEnd();
                                            }

                                            {
                                                /*
                                                      @Override
                                                      public Subscribers getSubscribers() {
                                                          return SUBSCRIBERS;
                                                      }
                                                 */
                                                MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getSubscribers", "()Lio/fairyproject/event/Subscribers;", null, null);
                                                methodVisitor.visitCode();
                                                Label label0 = new Label();
                                                methodVisitor.visitLabel(label0);
                                                methodVisitor.visitLineNumber(12, label0);
                                                methodVisitor.visitFieldInsn(GETSTATIC, classNode.name, "SUBSCRIBERS", "Lio/fairyproject/event/Subscribers;");
                                                methodVisitor.visitInsn(ARETURN);
                                                Label label1 = new Label();
                                                methodVisitor.visitLabel(label1);
                                                methodVisitor.visitLocalVariable("this", "L" + classNode.name + ";", null, label0, label1, 0);
                                                methodVisitor.visitMaxs(1, 1);
                                                methodVisitor.visitEnd();
                                            }
                                            super.visitEnd();
                                        }
                                    }

                                    classReader.accept(new SubscribersVisitor(classWriter), ClassReader.EXPAND_FRAMES);
                                    bytes = classWriter.toByteArray();
                                    break;
                                }
                            }
                        }
                    }

                    if (bytes == null) {
                        bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry));
                    }
                    out.write(bytes);
                }
            }

            if (mainClass == null) {
                System.out.println("No main class found, this project will be considered as a dependency project.");
            }

            for (PlatformType platformType : this.extension.getFairyPlatforms().get()) {
                final FileGenerator fileGenerator = platformType.createFileGenerator();
                if (fileGenerator == null)
                    continue;

                final Pair<String, byte[]> pair = fileGenerator.generate(this.extension, mainClass);
                out.putNextEntry(new JarEntry(pair.getLeft()));
                out.write(pair.getRight());
            }

            final Pair<String, byte[]> pair = new FileGeneratorFairy().generate(this.extension, mainClass);
            out.putNextEntry(new JarEntry(pair.getLeft()));
            out.write(pair.getRight());
        }
    }

}
