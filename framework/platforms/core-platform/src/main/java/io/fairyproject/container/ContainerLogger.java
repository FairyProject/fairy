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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ContainerLogger {

    public static void report(ContainerNode node, ContainerObj obj, @Nullable Throwable throwable, String... messages) {
        Log.error("!-------------------!");
        Log.error("An error was reported with Fairy container system!!");
        Log.error("Node: " + node.name());
        Log.error("Component: " + obj.getType().getName());
        Log.error(" ");
        Log.error("Messages:");
        for (String message : messages) {
            Log.error(">    " + message);
        }
        Path path = write(node, obj, messages, throwable);

        if (throwable != null) {
            Log.error(" ");
            Log.error("The error stacktrace: ");
            Log.error(Stacktrace.simplifyStacktrace(throwable));
        }

        Log.error(" ");
        if (path != null) {
            Log.error("The error has been written to the log file.");
            Log.error("Path: " + path.toAbsolutePath());
        }
        Log.error("!-------------------!");
    }

    public static Path write(ContainerNode node, @Nullable ContainerObj obj, @Nullable String[] messages, @Nullable Throwable throwable) {
        String time = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());

        Path directory = Paths.get("fairy");
        Path path = directory.resolve(time + ".log");
        int counter = 2;
        while (Files.exists(path)) {
            path = directory.resolve(time + "-" + counter + ".log");
            counter++;
        }

        try {
            Files.createDirectories(directory);

            StringWriter out = new StringWriter();
            PrintWriter builder = new PrintWriter(out);
            builder.append("Node: ").append(node.name()).append("\n");
            builder.append("Classes: ").append("\n");
            node.graph().forEachClockwise(chain -> {
                builder.append("-> ").append(chain.getType().getName()).append("\n");
            });

            if (obj != null) {
                builder.append(String.format("Node Object: %s\n", obj.getType().getName()));
                builder.append(String.format("  dependencies: %s\n", obj.getDependencies()));
                builder.append(String.format("  Component instance provider: %s\n", obj.getInstanceProvider()));
                builder.append(String.format("  Component thread mode: %s\n", obj.getThreadingMode()));
            }

            builder.append("--------------------\n");

            if (messages != null) {
                for (String message : messages) {
                    builder.append(message).append("\n");
                }
            }

            if (throwable != null) {
                throwable.printStackTrace(builder);
            }

            Files.write(path, out.toString().getBytes(), StandardOpenOption.CREATE_NEW);

            return path;
        } catch (IOException ex) {
            Log.error("Cannot write error log file", ex);
        }

        return null;
    }

}
