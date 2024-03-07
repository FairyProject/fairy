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

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.binder.ContainerObjectBinder;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.provider.InstanceProvider;
import io.fairyproject.container.object.provider.MethodInvokeInstanceProvider;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class ContainerNodeConfigurationScanner {

    private final ContainerObjectBinder binder;
    private final Class<?> configurationClass;
    private final boolean override;
    private final ContainerNodeClassScanner scanner;

    private Object instance;

    public void load() {
        this.createConfigurationInstance();

        for (Method method : configurationClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(InjectableComponent.class))
                this.loadConfigurationMethod(method, override);
        }
    }

    private void createConfigurationInstance() {
        try {
            Constructor<?> constructor = configurationClass.getDeclaredConstructor();

            this.instance = constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to create configuration instance.", ex);
        }
    }

    private void loadConfigurationMethod(Method method, boolean override) {
        InstanceProvider provider = new MethodInvokeInstanceProvider(this.instance, method);

        Class<?> javaClass = provider.getType();
        ContainerObj object = this.binder.getExactBinding(javaClass);

        if (object != null) {
            if (override) {
                object.setInstanceProvider(provider);
            }
        } else {
            object = scanner.createObject(javaClass);
            object.setInstanceProvider(provider);
        }

        ContainerObj previous = scanner.getNode().getObj(javaClass);
        if (previous != null) {
            if (override)
                return;
            throw new IllegalStateException("Component already exists: " + javaClass.getName());
        }

        InjectableComponent annotation = method.getAnnotation(InjectableComponent.class);
        if (annotation != null) {
            object.setScope(annotation.scope());
        }

        this.scanner.addComponentClass(object, scanner.getNode());
    }

}
