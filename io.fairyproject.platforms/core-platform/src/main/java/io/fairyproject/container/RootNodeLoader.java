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

package io.fairyproject.container;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.FairyPlatform;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.loader.ContainerNodeLoader;
import io.fairyproject.container.node.scanner.ContainerNodeClassScanner;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.log.Log;
import io.fairyproject.util.Stacktrace;
import lombok.RequiredArgsConstructor;

import static io.fairyproject.Debug.log;

@RequiredArgsConstructor
public class RootNodeLoader {

    private final ContainerContext context;
    private ContainerNode node;

    public ContainerNode load() {
        this.node = ContainerNode.create("root", context.containerObjectBinder());
        this.addPreDefinedComponents();
        this.runClassScanner();
        this.loadNode();

        return node;
    }

    private boolean loadNode() {
        return new ContainerNodeLoader(this.context, this.node).load();
    }

    private void runClassScanner() {
        try {
            ContainerNodeClassScanner classScanner = new ContainerNodeClassScanner(this.context, this.context.containerObjectBinder(), "framework", this.node);
            classScanner.getClassPaths().add(Fairy.getFairyPackage());
            if (!Debug.UNIT_TEST) {
                classScanner.getUrls().add(this.getClass().getProtectionDomain().getCodeSource().getLocation());
                classScanner.getClassLoaders().add(ContainerContext.class.getClassLoader());
            }

            classScanner.scan();
        } catch (Throwable throwable) {
            Log.error("Error while scanning classes for framework", Stacktrace.simplifyStacktrace(throwable));
            Fairy.getPlatform().shutdown();
        }
    }

    private void addPreDefinedComponents() {
        ContainerObj obj = ContainerObj.create(this.context.getClass());
        this.node.addObj(obj);
        this.context.containerObjectBinder().bind(ContainerContext.class, obj);
        this.context.singletonObjectRegistry().registerSingleton(ContainerContext.class, this.context);

        log("ContainerContext has been registered as ContainerObject.");

        obj = ContainerObj.create(FairyPlatform.class);
        this.node.addObj(obj);
        this.context.containerObjectBinder().bind(FairyPlatform.class, obj);
        this.context.singletonObjectRegistry().registerSingleton(FairyPlatform.class, Fairy.getPlatform());

        log("FairyPlatform has been registered as ContainerObject.");
    }

}
