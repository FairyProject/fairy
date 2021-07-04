/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.fairy.bean.details;

import lombok.Getter;
import lombok.Setter;
import org.fairy.bean.details.constructor.BeanParameterDetailsConstructor;
import org.fairy.bean.BeanContext;
import org.fairy.bean.DependencyType;
import org.fairy.bean.ServiceDependency;
import org.fairy.bean.ServiceDependencyType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collection;

@Getter
@Setter
public class ServiceBeanDetails extends DependenciesBeanDetails {

    private BeanParameterDetailsConstructor constructorDetails;

    public ServiceBeanDetails(Class<?> type, String name, String[] dependencies) {
        super(type, name, dependencies);

        this.loadAnnotations();
    }

    public void setupConstruction(BeanContext beanContext) {
        this.constructorDetails = new BeanParameterDetailsConstructor(this.getType(), beanContext);
        for (Parameter parameter : this.constructorDetails.getParameters()) {
            BeanDetails details = beanContext.getBeanDetails(parameter.getType());

            ServiceDependencyType type = ServiceDependencyType.FORCE;
            final DependencyType annotation = parameter.getAnnotation(DependencyType.class);
            if (annotation != null) {
                type = annotation.value();
            }
            this.addDependencies(type, details.getName());
        }
    }

    public void build(BeanContext context) {
        if (this.constructorDetails == null) {
            throw new IllegalArgumentException("The construction for bean details " + this.getType().getName() + " hasn't been called!");
        }

        try {
            this.setInstance(this.constructorDetails.newInstance(context));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadAnnotations(Collection<Class<?>> superClasses) {
        super.loadAnnotations(superClasses);

        for (Class<?> type : superClasses) {
            for (ServiceDependency dependency : type.getAnnotationsByType(ServiceDependency.class)) {
                this.addDependencies(dependency.type().value(), dependency.dependencies());
            }
        }
    }
}
