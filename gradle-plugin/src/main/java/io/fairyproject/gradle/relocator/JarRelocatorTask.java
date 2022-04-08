/*
 * Copyright Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fairyproject.gradle.relocator;

import com.google.common.io.ByteStreams;
import io.fairyproject.gradle.util.ParallelThreadingUtil;
import org.gradle.jvm.tasks.Jar;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.*;
import java.util.regex.Pattern;

/**
 * A task that copies {@link JarEntry jar entries} from a {@link JarFile jar input} to a
 * {@link JarOutputStream jar output}, applying the relocations defined by a
 * {@link RelocatingRemapper}.
 */
final class JarRelocatorTask {

    /**
     * META-INF/*.SF
     * META-INF/*.DSA
     * META-INF/*.RSA
     * META-INF/SIG-*
     *
     * <a href="https://docs.oracle.com/en/java/javase/11/docs/specs/jar/jar.html#signed-jar-file">Specification</a>
     */
    private static final Pattern SIGNATURE_FILE_PATTERN = Pattern.compile("META-INF/(?:[^/]+\\.(?:DSA|RSA|SF)|SIG-[^/]+)");

    /**
     * <a href="https://docs.oracle.com/en/java/javase/11/docs/specs/jar/jar.html#signature-validation">Specification</a>
     */
    private static final Pattern SIGNATURE_PROPERTY_PATTERN = Pattern.compile(".*-Digest");

    private final RelocatingRemapper remapper;
    private final JarOutputStream jarOut;
    private final JarFile jarIn;
    private final Set<File> relocateEntries;

    private final ReentrantLock lock = new ReentrantLock();

    private final Set<String> classes = ConcurrentHashMap.newKeySet();
    private final Set<String> resources = ConcurrentHashMap.newKeySet();

    JarRelocatorTask(RelocatingRemapper remapper, JarOutputStream jarOut, JarFile jarIn, Set<File> relocateEntries) {
        this.remapper = remapper;
        this.jarOut = jarOut;
        this.jarIn = jarIn;
        this.relocateEntries = relocateEntries;
    }

    public boolean isExistingEntry(String className) {
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }
        String finalClassName = className;
        return classes.stream().anyMatch(finalClassName::startsWith);
    }

    void processEntries() throws IOException {
        this.remapper.setTask(this);

        // Cache existing classes
        ParallelThreadingUtil.invokeAll(this.jarIn.entries(), entry -> {
            if (!entry.getName().endsWith(".class")) {
                return;
            }
            try {
                ClassReader classReader = new ClassReader(ByteStreams.toByteArray(jarIn.getInputStream(entry)));
                ClassNode classNode = new ClassNode();

                classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                classes.add(classNode.name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Cache targeted entries classes
        for (File file : this.relocateEntries) {
            try (JarFile in = new JarFile(file)) {
                ParallelThreadingUtil.invokeAll(in.entries(), entry -> {
                    if (!entry.getName().endsWith(".class")) {
                        return;
                    }
                    try {
                        ClassReader classReader = new ClassReader(ByteStreams.toByteArray(jarIn.getInputStream(entry)));
                        ClassNode classNode = new ClassNode();

                        classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                        classes.add(classNode.name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        ParallelThreadingUtil.<JarEntry>newTask()
                .collection(this.jarIn.entries())
                .syncConsumer(entry -> {
                    // The 'INDEX.LIST' file is an optional file, containing information about the packages
                    // defined in a jar. Instead of relocating the entries in it, we delete it, since it is
                    // optional anyway.
                    //
                    // We don't process directory entries, and instead opt to recreate them when adding
                    // classes/resources.
                    String name = entry.getName();
                    if (name.equals("META-INF/INDEX.LIST") || entry.isDirectory()) {
                        return false;
                    }

                    // Signatures will become invalid after remapping, so we delete them to avoid making the output useless
                    if (SIGNATURE_FILE_PATTERN.matcher(name).matches()) {
                        return false;
                    }

                    this.processEntrySync(entry);
                    return true;
                })
                .asyncConsumer(entry -> {
                    try (InputStream entryIn = this.jarIn.getInputStream(entry)) {
                        processEntry(entry, entryIn);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                })
                .start();

        this.remapper.setTask(null);
    }

    private void processEntrySync(JarEntry entry) {
        String name = entry.getName();
        String mappedName = this.remapper.map(name);

        // ensure the parent directory structure exists for the entry.
//        processDirectorySync(mappedName, true);

        if (!name.endsWith(".class")) {
            if (name.equals("META-INF/MANIFEST.MF") || !this.resources.contains(mappedName)) {
                this.resources.add(name);
            }
        }
    }

//    private void processDirectorySync(String name, boolean parentOnly) {
//        int index = name.lastIndexOf('/');
//        if (index != -1) {
//            String parentDirectory = name.substring(0, index);
//            if (!this.resources.contains(parentDirectory)) {
//                processDirectorySync(parentDirectory, false);
//            }
//        }
//
//        if (parentOnly) {
//            return;
//        }
//
//        this.resources.add(name);
//    }

    private void processEntry(JarEntry entry, InputStream entryIn) throws IOException {
        String name = entry.getName();
        String mappedName = this.remapper.map(name);

        // ensure the parent directory structure exists for the entry.
//        processDirectory(mappedName);

        if (name.endsWith(".class")) {
            processClass(name, entryIn);
        } else if (name.equals("META-INF/MANIFEST.MF")) {
            processManifest(name, entryIn, entry.getTime());
        } else {
            processResource(mappedName, entryIn, entry.getTime());
        }
    }

//    private void processDirectory(String name) throws IOException {
//        // directory entries must end in "/"
//        JarEntry entry = new JarEntry(name + "/");
//        this.lock.lock();
//        try {
//            this.jarOut.putNextEntry(entry);
//        } finally {
//            this.lock.unlock();
//        }
//    }

    private void processManifest(String name, InputStream entryIn, long lastModified) throws IOException {
        Manifest in = new Manifest(entryIn);
        Manifest out = new Manifest();

        out.getMainAttributes().putAll(in.getMainAttributes());

        for (Map.Entry<String, Attributes> entry : in.getEntries().entrySet()) {
            Attributes outAttributes = new Attributes();
            for (Map.Entry<Object, Object> property : entry.getValue().entrySet()) {
                String key = property.getKey().toString();
                if (!SIGNATURE_PROPERTY_PATTERN.matcher(key).matches()) {
                    outAttributes.put(property.getKey(), property.getValue());
                }
            }
            out.getEntries().put(entry.getKey(), outAttributes);
        }

        JarEntry jarEntry = new JarEntry(name);
        jarEntry.setTime(lastModified);

        this.lock.lock();
        try {
            this.jarOut.putNextEntry(jarEntry);
            out.write(this.jarOut);
        } finally {
            this.lock.unlock();
        }
    }

    private void processResource(String name, InputStream entryIn, long lastModified) throws IOException {
        JarEntry jarEntry = new JarEntry(name);
        jarEntry.setTime(lastModified);

        this.lock.lock();
        try {
            this.jarOut.putNextEntry(jarEntry);
            copy(entryIn, this.jarOut);
        } finally {
            this.lock.unlock();
        }
    }

    private void processClass(String name, InputStream entryIn) throws IOException {
        ClassReader classReader = new ClassReader(entryIn);
        ClassWriter classWriter = new ClassWriter(0);
        RelocatingClassVisitor classVisitor = new RelocatingClassVisitor(classWriter, this.remapper, name);

        try {
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        } catch (Throwable e) {
            throw new RuntimeException("Error processing class " + name, e);
        }

        byte[] renamedClass = classWriter.toByteArray();

        // Need to take the .class off for remapping evaluation
        String mappedName = this.remapper.map(name.substring(0, name.indexOf('.')));

        // Now we put it back on so the class file is written out with the right extension.
        this.lock.lock();
        try {
            this.jarOut.putNextEntry(new JarEntry(mappedName + ".class"));
            this.jarOut.write(renamedClass);
        } finally {
            this.lock.unlock();
        }
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[8192];
        while (true) {
            int n = from.read(buf);
            if (n == -1) {
                break;
            }
            to.write(buf, 0, n);
        }
    }
}
