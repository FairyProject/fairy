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

package io.fairyproject.container.scope;

/**
 * Enum that represents the different scopes that an injectable component can have.
 *
 * @author LeeGod
 */
public enum InjectableScope {
    /**
     * Indicates that the injectable component is a singleton, and only one instance of the component
     * will be created and managed by the dependency injection container.
     */
    SINGLETON,

    /**
     * Indicates that the injectable component is a prototype, and a new instance of the component
     * will be created each time it is injected.
     * <p>
     * Lifecycle of prototype scoped components is not managed by the dependency injection container.
     * PRE_INIT and POST_INIT will be called, but PRE_DESTROY and POST_DESTROY will not be called.
     * PRE_INIT and POST_INIT is called everytime the instance were created, that means PRE_INIT MAY be called after current node is loaded.
     * It can happen for example if somehow a new node containing an object that is depending on a prototype scoped component of an already loaded node.
     */
    PROTOTYPE
}