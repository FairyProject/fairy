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

package io.fairyproject.bean;

import io.fairyproject.bean.details.constructor.BeanParameterDetailsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class ComponentHolder {

    public Object newInstance(Class<?> type) {
        return this.newInstance(this.constructorDetails(type));
    }

    public Object newInstance(BeanParameterDetailsConstructor constructorDetails) {
        try {
            return constructorDetails.newInstance(BeanContext.INSTANCE);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void onEnable(Object instance) {

    }

    // TODO ?
    public void onDisable(Object instance) {

    }

    public abstract Class<?>[] type();

    public BeanParameterDetailsConstructor constructorDetails(Class<?> type) {
        return new BeanParameterDetailsConstructor(type, BeanContext.INSTANCE);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Class<?>> types = new ArrayList<>();
        private Consumer<Object> onEnable;
        private Consumer<Object> onDisable;

        public Builder onEnable(Consumer<Object> onEnable) {
            this.onEnable = onEnable;
            return this;
        }

        public Builder onDisable(Consumer<Object> onDisable) {
            this.onDisable = onDisable;
            return this;
        }

        public Builder type(Class<?>... types) {
            this.types.addAll(Arrays.asList(types));
            return this;
        }

        public ComponentHolder build() {
            final Class<?>[] types = this.types.toArray(new Class<?>[0]);

            return new ComponentHolder() {
                @Override
                public Class<?>[] type() {
                    return types;
                }

                @Override
                public void onEnable(Object instance) {
                    if (onEnable != null)
                        onEnable.accept(instance);
                }

                @Override
                public void onDisable(Object instance) {
                    if (onDisable != null)
                        onDisable.accept(instance);
                }
            };
        }

    }

}
