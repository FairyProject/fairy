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

package io.fairyproject.container.node.scanner;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.Register;
import io.fairyproject.container.Service;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.Obj;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.container.object.provider.MethodInvokeInstanceProvider;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class ContainerNodeLegacyScanner {

    private final ScanResult scanResult;
    private final ContainerContext context;
    private final ContainerNodeClassScanner scanner;

    void load() {
        this.loadServiceClasses();
        this.loadObjClasses();
        this.loadRegisterMethods();
    }

    private void loadServiceClasses() {
        ClassInfoList serviceClasses = scanResult.getClassesWithAnnotation(Service.class);
        scanner.loadComponentClasses(serviceClasses, scanner.getNode(), $ -> {});
    }

    private void loadObjClasses() {
        ClassInfoList objClasses = scanResult.getClassesWithAnnotation(Obj.class);
        scanner.loadComponentClasses(objClasses, scanner.getObjNode(), $ -> {});
    }

    private void loadRegisterMethods() {
        ClassInfoList registerMethodClasses = scanResult.getClassesWithMethodAnnotation(Register.class);

        for (ClassInfo classInfo : registerMethodClasses) {
            for (MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()) {
                this.loadRegisterMethod(methodInfo);
            }
        }
    }

    private void loadRegisterMethod(MethodInfo methodInfo) {
        if (!methodInfo.hasAnnotation(Register.class) || !methodInfo.isStatic())
            return;

        Method method = methodInfo.loadClassAndGetMethod();
        InstanceProvider instanceProvider = new MethodInvokeInstanceProvider(null, method);
        Class<?> type = instanceProvider.getType();

        ContainerObj object = context.containerObjectBinder().getBinding(type);
        if (object == null) {
            object = scanner.createObject(type);
            object.setInstanceProvider(instanceProvider);
        }

        scanner.addComponentClass(object, scanner.getNode());
    }

}
