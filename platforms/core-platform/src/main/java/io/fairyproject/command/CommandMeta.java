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

package io.fairyproject.command;

import io.fairyproject.command.parameter.ParameterMeta;
import io.fairyproject.util.Stacktrace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class CommandMeta {

    private final String[] names;
    private final String permission;
    private final List<ParameterMeta> parameters;
    private final Object instance;
    private final Method method;
    private final PresenceProvider presenceProvider;

    public String getName() {
        return names[0];
    }

    public boolean canAccess(CommandEvent commandEvent) {
        if (this.permission == null || this.permission.length() == 0) {
            return true;
        }

        return commandEvent.hasPermission(this.permission);
    }

    public String getUsage() {
        return this.getUsage(this.getName());
    }

    public String getUsage(String aliasUsed) {
        StringBuilder stringBuilder = new StringBuilder();

        for (ParameterMeta parameterMeta : getParameters()) {
            boolean needed = parameterMeta.getDefaultValue().isEmpty();
            stringBuilder
                    .append(needed ? "<" : "[").append(parameterMeta.getName())
                    .append(needed ? ">" : "]").append(" ");
        }

        return ("/" + aliasUsed.toLowerCase() + " " + stringBuilder.toString().trim().toLowerCase());
    }

    public void execute(CommandEvent event, String[] arguments) {
        if (!method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
            event.sendInternalError("This command cannot be executed by " + event.name());
            return;
        }

        List<Object> transformedParameters = new ArrayList<>();

        transformedParameters.add(event);

        for (int i = 0; i < this.getParameters().size(); i++) {
            ParameterMeta parameter = getParameters().get(i);
            String passedParameter = (i < arguments.length ? arguments[i] : parameter.getDefaultValue()).trim();
            if (i >= arguments.length &&
                    (parameter.getDefaultValue() == null || parameter.getDefaultValue().isEmpty())) {
                event.sendUsage(this.getUsage());
                return;
            }
            if (parameter.isWildcard() && !passedParameter.trim().equals(parameter.getDefaultValue().trim())) {
                passedParameter = toString(arguments, i);
            }
            Object result = CommandService.INSTANCE.transformParameter(event, passedParameter, parameter.getParameterClass());
            if (result == null) {
                event.sendUsage(this.getUsage());
                return;
            }
            transformedParameters.add(result);
            if (parameter.isWildcard()) {
                break;
            }
        }

        try {
            method.invoke(this.instance, transformedParameters.toArray());
        } catch (Exception e) {
            event.sendError(e);
            Stacktrace.print(e);
        }
    }

    public static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int arg = start; arg < args.length; arg++) {
            stringBuilder.append(args[arg]).append(" ");
        }

        return (stringBuilder.toString().trim());
    }

}
