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

package io.fairyproject.bukkit.scheduler.folia.wrapper;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class WrapperScheduledTask {

    public static WrapperScheduledTask of(Object scheduledTask) {
        return new WrapperScheduledTask(scheduledTask);
    }

    private static Method cancelMethod;

    private final Object scheduledTask;

    public Class<?> getTaskClass() {
        return scheduledTask.getClass();
    }

    public void cancel() {
        if (cancelMethod == null) {
            Class<?> scheduledTaskClass = scheduledTask.getClass();
            try {
                cancelMethod = scheduledTaskClass.getMethod("cancel");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Cannot find cancel method in " + scheduledTaskClass.getName(), e);
            }
        }

        try {
            cancelMethod.invoke(scheduledTask);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot invoke cancel method in " + scheduledTask.getClass().getName(), e);
        }
    }

}
