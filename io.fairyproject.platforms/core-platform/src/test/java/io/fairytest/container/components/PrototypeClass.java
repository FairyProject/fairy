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

package io.fairytest.container.components;

import io.fairyproject.container.*;
import io.fairyproject.container.scope.InjectableScope;
import lombok.Getter;

@InjectableComponent(scope = InjectableScope.PROTOTYPE)
@Getter
public class PrototypeClass {

    private final SingletonClass singleton;

    @Autowired
    private InterfaceClass singletonAutowired;

    private final Thread mainThreadConstruct;
    private Thread mainThreadPreInit;
    private Thread mainThreadPostInit;

    private final long constructTime;
    private long preInitTime;
    private long postInitTime;

    public PrototypeClass(SingletonClass singleton) {
        this.singleton = singleton;

        this.mainThreadConstruct = Thread.currentThread();
        this.constructTime = System.nanoTime();
    }

    @PreInitialize
    public void preInit() {
        this.mainThreadPreInit = Thread.currentThread();
        this.preInitTime = System.nanoTime();
    }

    @PostInitialize
    public void postInit() {
        this.mainThreadPostInit = Thread.currentThread();
        this.postInitTime = System.nanoTime();
    }

    @PreDestroy
    public void preDestroy() {
        throw new IllegalStateException("PreDestroy should not be called on PrototypeClass");
    }

    @PostDestroy
    public void postDestroy() {
        throw new IllegalStateException("PostDestroy should not be called on PrototypeClass");
    }

}
