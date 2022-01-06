package io.fairyproject.gradle;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

@Getter
@Setter
public class FairyTestTask extends DefaultTask {

    @Input
    private FairyExtension extension;

    @TaskAction
    public void afterTestClasses() throws Exception {
        final SourceSetContainer sourceSets = this.getProject().getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
        final SourceSet testSourceSet = sourceSets.findByName("test");

        if (testSourceSet == null) {
            return;
        }

        final File directory = testSourceSet.getOutput().getClassesDirs().getSingleFile();
        final File moduleClass = new File(directory, "MODULE.class");

        Files.write(moduleClass.toPath(), this.dump());
    }

    private byte[] dump() {
        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "MODULE", null, "java/lang/Object", null);

        classWriter.visitSource("MODULE.java", null);

        {
            fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_STATIC, "ALL", "Ljava/util/ArrayList;", "Ljava/util/ArrayList<Ljava/lang/String;>;", null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "LMODULE;", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(5, label0);
            methodVisitor.visitTypeInsn(NEW, "java/util/ArrayList");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            methodVisitor.visitFieldInsn(PUTSTATIC, "MODULE", "ALL", "Ljava/util/ArrayList;");

            for (Map.Entry<String, String> entry : this.extension.getFairyModules().entrySet()) {
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLineNumber(8, label1);
                methodVisitor.visitFieldInsn(GETSTATIC, "MODULE", "ALL", "Ljava/util/ArrayList;");
                methodVisitor.visitLdcInsn("io.fairyproject:" + entry.getKey() + ":" + entry.getValue());
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
                methodVisitor.visitInsn(POP);
            }

            Label label6 = new Label();
            methodVisitor.visitLabel(label6);
            methodVisitor.visitLineNumber(13, label6);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 0);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

}
