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

package io.fairyproject.util.terminable;

import io.fairyproject.util.terminable.module.TerminableModule;
import org.jetbrains.annotations.NotNull;

/**
 * Accepts {@link Terminable},
 * as well as {@link TerminableModule}s.
 *
 * @author lucko
 */
@FunctionalInterface
public interface TerminableConsumer {

    /**
     * Binds with the given terminable.
     *
     * @param terminable the terminable to bind with
     * @param <T> the terminable type
     * @return the same terminable
     */
    @NotNull
    <T extends Terminable> T bind(@NotNull T terminable);

    /**
     * Binds with the given terminable module.
     *
     * @param module the module to bind with
     * @param <T> the module type
     * @return the same module
     */
    @NotNull
    default <T extends TerminableModule> T bindModule(@NotNull T module) {
        module.setup(this);
        return module;
    }

}
