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

import io.fairyproject.container.Register;
import io.fairyproject.container.Service;
import io.fairyproject.container.ServiceDependency;
import io.fairyproject.container.ServiceDependencyType;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.Obj;
import io.fairyproject.container.object.provider.MethodInvokeInstanceProvider;
import io.fairyproject.container.object.resolver.MethodContainerResolver;
import io.fairyproject.util.Utility;
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
    private final ContainerNodeClassScanner scanner;

    void load() {
        this.loadServiceClasses();
        this.loadObjClasses();
        this.loadRegisterMethods();
    }

    private void loadServiceClasses() {
        ClassInfoList serviceClasses = scanResult.getClassesWithAnnotation(Service.class);
        scanner.loadComponentClasses(serviceClasses, scanner.getNode(), obj -> {
            Service service = obj.type().getDeclaredAnnotation(Service.class);

            for (Class<?> depend : service.depends()) {
                obj.addDepend(depend, ServiceDependencyType.FORCE);
            }

            this.addDependFromAnnotation(obj);
        });
    }

    private void loadObjClasses() {
        ClassInfoList objClasses = scanResult.getClassesWithAnnotation(Obj.class);
        scanner.loadComponentClasses(objClasses, scanner.getObjNode(), this::addDependFromAnnotation);
    }

    private void addDependFromAnnotation(ContainerObj obj) {
        for (Class<?> superClass : Utility.getSuperAndInterfaces(obj.type())) {
            ServiceDependency annotation = superClass.getDeclaredAnnotation(ServiceDependency.class);
            if (annotation != null) {
                for (Class<?> depend : annotation.value()) {
                    obj.addDepend(depend, annotation.type());
                }
            }
        }
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
        MethodContainerResolver resolver = new MethodContainerResolver(method, scanner.getContext());

        ContainerObj containerObj = scanner.addComponentClass(resolver.returnType(), scanner.getNode());
        containerObj.setProvider(new MethodInvokeInstanceProvider(null, resolver));
    }

}
