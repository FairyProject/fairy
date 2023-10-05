/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.container.node.loader;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.scanner.ContainerNodeClassScanner;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.util.Stacktrace;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PluginNodeLoader {

    private final ContainerContext context;
    private final Plugin plugin;

    private ContainerNode node;

    public ContainerNode load() {
        this.node = ContainerNode.create(this.plugin.getName(), context.containerObjectBinder());

        this.addPluginAsComponent();
        this.runClassScanner();
        this.loadNode();

        return node;
    }

    private boolean loadNode() {
        return new ContainerNodeLoader(this.context, this.node).load();
    }

    private void addPluginAsComponent() {
        ContainerObj pluginObj = ContainerObj.create(this.plugin.getClass());
        context.singletonObjectRegistry().registerSingleton(this.plugin.getClass(), this.plugin);
        context.containerObjectBinder().bind(this.plugin.getClass(), pluginObj);
        node.addObj(pluginObj);

        Debug.log("Plugin " + plugin.getName() + " has been registered as ContainerObject.");
    }

    private void runClassScanner() {
        try {
            ContainerNodeClassScanner classScanner = new ContainerNodeClassScanner(this.context, this.context.containerObjectBinder(), this.plugin.getName(), this.node);
            classScanner.getClassLoaders().add(plugin.getPluginClassLoader());
            classScanner.getClassPaths().addAll(this.findClassPaths());
            classScanner.getExcludedClassPaths().add(Fairy.getFairyPackage());
            if (Debug.UNIT_TEST) {
                // Hard coded, anyway to make it safer?
                Path pathMain = Paths.get("build/classes/java/main").toAbsolutePath();
                if (Files.exists(pathMain))
                    classScanner.getUrls().add(pathMain.toUri().toURL());

                Path pathTest = Paths.get("build/classes/java/test").toAbsolutePath();
                if (Files.exists(pathTest))
                    classScanner.getUrls().add(pathTest.toUri().toURL());

                pathMain = Paths.get("build/classes/kotlin/main").toAbsolutePath();
                if (Files.exists(pathMain))
                    classScanner.getUrls().add(pathMain.toUri().toURL());

                pathTest = Paths.get("build/classes/kotlin/test").toAbsolutePath();
                if (Files.exists(pathTest))
                    classScanner.getUrls().add(pathTest.toUri().toURL());
            } else {
                classScanner.getUrls().add(plugin.getClass().getProtectionDomain().getCodeSource().getLocation());
            }

            classScanner.scan();
        } catch (Throwable throwable) {
            Log.error("Error while scanning classes for framework", Stacktrace.simplifyStacktrace(throwable));
            Fairy.getPlatform().shutdown();
        }
    }

    private List<String> findClassPaths() {
        List<String> classPaths = new ArrayList<>(this.context.findClassPaths(this.plugin.getClass()));
        classPaths.add(plugin.getDescription().getShadedPackage());

        return classPaths;
    }

}
