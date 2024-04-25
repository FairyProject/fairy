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


import io.fairyproject.container.scope.InjectableScope;

import java.lang.annotation.*;

/**
 * Annotation used to mark a class or method as a candidate for dependency injection.
 * When used to annotate a class, the class will be added to a container for
 * dependency injection. When used to annotate a method, an instance of the return
 * type will be created and placed into a container when the method is called.
 *
 * <p>Inject by class:</p>
 * <pre>
 *   {@code
 *   @InjectableComponent
 *   public class Service {
 *     // class implementation
 *   }
 *
 *   @InjectableComponent
 *   public class OtherComponent {
 *     private final Service service;
 *
 *     // constructor injection
 *     public OtherComponent(Service service) {
 *       this.service = service;
 *     }
 *   }
 *   }
 * </pre>
 *
 * <p>Inject by configuration method:</p>
 * <pre>
 *   {@code
 *   public class Service {
 *     // class implementation
 *   }
 *
 *   @Configuration
 *   public class MyConfiguration {
 *     @InjectableComponent
 *     public Service provideService() {
 *       return new Service();
 *     }
 *   }
 *   }
 * </pre>
 *
 * @author LeeGod
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectableComponent {

    InjectableScope scope() default InjectableScope.SINGLETON;

}
