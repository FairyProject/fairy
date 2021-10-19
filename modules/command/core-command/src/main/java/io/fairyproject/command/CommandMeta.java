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

import io.fairyproject.bean.Autowired;
import io.fairyproject.command.annotation.Arg;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.command.annotation.CommandPresence;
import io.fairyproject.command.annotation.TabCompletion;
import io.fairyproject.command.parameter.ParameterMeta;
import io.fairyproject.command.util.TabCompleteUtil;
import io.fairyproject.reflect.Reflect;
import io.fairyproject.util.Stacktrace;
import lombok.Getter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class CommandMeta {

    @Autowired
    private static CommandService COMMAND_SERVICE;

    private final String[] names;
    private final String permission;
    private final List<ParameterMeta> parameters;
    private final BaseCommand baseCommand;
    private final Class<?> contextClass;
    private final MethodHandle method;
    private final PresenceProvider presenceProvider;

    private int requireInputParameterCount;
    private int optionalInputParameterCount;

    public CommandMeta(Command annotation, Method method, BaseCommand command) throws IllegalAccessException {
        this.parameters = new ArrayList<>();
        this.baseCommand = command;
        this.method = Reflect.lookup().unreflect(method);
        Class<?>[] parametersClasses = method.getParameterTypes();

        this.contextClass = parametersClasses[0];

        // Offset of 1 here for the sender parameter.
        for (int parameterIndex = 1; parameterIndex < parametersClasses.length; parameterIndex++) {
            java.lang.reflect.Parameter parameter = method.getParameters()[parameterIndex];
            Arg argAnnotation = parameter.getAnnotation(Arg.class);

            String[] tabCompletion = new String[0];
            TabCompletion tabCompletionAnnotation = parameter.getAnnotation(TabCompletion.class);
            if (tabCompletionAnnotation != null) {
                tabCompletion = tabCompletionAnnotation.value();
            }

            final ParameterMeta parameterMeta;
            if (argAnnotation != null) {
                parameterMeta = new ParameterMeta(argAnnotation.value(), argAnnotation.wildcard(), argAnnotation.defaultValue(), argAnnotation.tabCompleteFlags(), tabCompletion, parameter.getType());
            } else {
                parameterMeta = new ParameterMeta(parameter.getName(), false, "", new String[]{""}, tabCompletion, parameter.getType());
            }

            this.parameters.add(parameterMeta);
            if (parameterMeta.getDefaultValue().isEmpty()) {
                this.requireInputParameterCount++;
            } else {
                this.optionalInputParameterCount++;
            }
        }
        PresenceProvider<?> presenceProviderMethod = null;
        CommandPresence annotationMethod = method.getAnnotation(CommandPresence.class);
        if (annotationMethod != null) {
            presenceProviderMethod = COMMAND_SERVICE.getPresenceProviderByAnnotation(annotationMethod);
        }
        if (presenceProviderMethod == null) {
            presenceProviderMethod = COMMAND_SERVICE.getPresenceProviderByType(parametersClasses[0]);
        }

        if (presenceProviderMethod == null) {
            throw new IllegalArgumentException("The method " + method + " with first parameters " + parametersClasses[0].getSimpleName() + " doesn't have match presence provider!");
        }

        if (!parametersClasses[0].isAssignableFrom(presenceProviderMethod.type())) {
            throw new IllegalArgumentException("The method " + method + " with first parameters " + parametersClasses[0].getSimpleName() + " doesn't match to " + presenceProviderMethod.getClass().getSimpleName() + " (requires type: " + presenceProviderMethod.type().getSimpleName() + ")");
        }

        this.names = annotation.value();
        this.permission = annotation.permissionNode();
        this.presenceProvider = presenceProviderMethod;
    }

    public int getParameterCount() {
        return this.parameters.size();
    }

    public String getName() {
        return names[0];
    }

    public boolean canAccess(CommandContext commandContext) {
        if (this.permission == null || this.permission.length() == 0) {
            return true;
        }

        return commandContext.hasPermission(this.permission);
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

        return "/" + this.baseCommand.getCommandNames()[0] + " " + aliasUsed.toLowerCase() + " " + stringBuilder.toString().trim().toLowerCase();
    }

    public void execute(CommandContext commandContext) {
        if (this.presenceProvider != null)
            commandContext.setPresenceProvider(this.presenceProvider);

        final String[] arguments = commandContext.getArgs();
        if (!this.contextClass.isAssignableFrom(commandContext.getClass())) {
            commandContext.sendMessage(MessageType.ERROR, "This command cannot be executed by " + commandContext.name());
            return;
        }

        List<Object> transformedParameters = new ArrayList<>();

        transformedParameters.add(commandContext);

        for (int i = 0; i < this.getParameters().size(); i++) {
            ParameterMeta parameter = getParameters().get(i);
            String passedParameter = (i < arguments.length ? arguments[i] : parameter.getDefaultValue()).trim();
            if (i >= arguments.length &&
                    (parameter.getDefaultValue() == null || parameter.getDefaultValue().isEmpty())) {
                commandContext.sendMessage(MessageType.INFO, this.getUsage());
                return;
            }
            if (parameter.isWildcard() && !passedParameter.trim().equals(parameter.getDefaultValue().trim())) {
                passedParameter = toString(arguments, i);
            }
            Object result = CommandService.INSTANCE.transformParameter(commandContext, passedParameter, parameter.getParameterClass());
            if (result == null) {
                commandContext.sendMessage(MessageType.INFO, this.getUsage());
                return;
            }
            transformedParameters.add(result);
            if (parameter.isWildcard()) {
                break;
            }
        }

        try {
            this.method.invoke(this.baseCommand, transformedParameters.toArray());
        } catch (Throwable e) {
            commandContext.sendMessage(MessageType.ERROR, "Exception thrown: " + e.getMessage());
            Stacktrace.print(e);
        }
    }

    public List<String> completeCommand(CommandContext commandContext) {
        final String[] args = commandContext.getArgs();
        if (!this.canAccess(commandContext) || args.length == 0 || this.getParameterCount() == 0) {
            return Collections.emptyList();
        }

        if (!this.parameters.get(this.parameters.size() - 1).isWildcard() && args.length > this.getParameterCount()) {
            return Collections.emptyList();
        }

        final ParameterMeta parameterMeta = this.parameters.get(args.length - 1);
        if (parameterMeta.getTabCompletion().length > 0) {
            return TabCompleteUtil.filterTabComplete(args[args.length - 1], Arrays.asList(parameterMeta.getTabCompletion()));
        }
        List<String> tabCompletes = COMMAND_SERVICE.tabCompleteParameters(commandContext, args[args.length - 1], parameterMeta.getParameterClass(), parameterMeta.getTabCompleteFlags());
        return TabCompleteUtil.filterTabComplete(args[args.length - 1], tabCompletes);
    }

    public static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int arg = start; arg < args.length; arg++) {
            stringBuilder.append(args[arg]).append(" ");
        }

        return (stringBuilder.toString().trim());
    }

}
