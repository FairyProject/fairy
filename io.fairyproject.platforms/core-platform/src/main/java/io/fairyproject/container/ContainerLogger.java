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

import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.log.Log;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class ContainerLogger {

    public static void reportComponent(@NotNull ContainerObj obj, @Nullable String message, @Nullable Throwable throwable) {
        // write as much information about obj as possible
        if (message != null) {
            // write message
            Log.error(String.format("Component %s: %s", obj.type().getName(), message));
        } else {
            Log.error(String.format("Component %s", obj.type().getName()));
        }

        Log.error(String.format("Component life cycle: %s", obj.lifeCycle()));
        Log.error(String.format("Component dependencies: %s", obj.dependEntries()));
        Log.error(String.format("Component instance provider: %s", obj.provider()));
        Log.error(String.format("Component instance: %s", obj.instance()));
        Log.error(String.format("Component thread mode: %s", obj.threadingMode()));
        Log.error(String.format("Component life cycle handlers: %s", obj.lifeCycleHandlers().stream()
                .map(handler -> handler.getClass().getName())
                .collect(Collectors.toList())));

        if (throwable != null) {
            SneakyThrowUtil.sneakyThrow(Stacktrace.simplifyStacktrace(throwable));
        }
    }

    public static void reportNode(@NotNull ContainerNode node) {
        Log.error("Node: " + node.name());
        Log.error(String.format("Node classes: %s", node.all().stream()
                .map(obj -> obj.type().getName())
                .collect(Collectors.toList())
        ));
    }

}
