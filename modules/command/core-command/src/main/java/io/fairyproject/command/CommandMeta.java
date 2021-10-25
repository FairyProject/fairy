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
import io.fairyproject.command.annotation.*;
import io.fairyproject.command.argument.ArgCompletionHolder;
import io.fairyproject.command.argument.ArgMeta;
import io.fairyproject.command.exception.ArgTransformException;
import io.fairyproject.command.util.CoreCommandUtil;
import io.fairyproject.util.RV;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.StringUtil;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class CommandMeta implements ICommand {

    @Autowired
    private static CommandService COMMAND_SERVICE;

    private final String[] names;
    private final String permission;
    private final String usage;
    private final List<ArgMeta> arguments;
    private final BaseCommand baseCommand;
    private final Class<?> contextClass;
    private final Method method;
    private final PresenceProvider presenceProvider;

    private int requireInputParameterCount;
    private int maxParameterCount;

    public CommandMeta(Command annotation, Method method, BaseCommand command) throws IllegalAccessException {
        this.arguments = new ArrayList<>();
        this.baseCommand = command;
        this.method = method;
        Class<?>[] parametersClasses = method.getParameterTypes();

        this.contextClass = parametersClasses[0];

        int skippedCount = 0;
        // Offset of 1 here for the sender parameter.
        for (int parameterIndex = 1; parameterIndex < parametersClasses.length; parameterIndex++) {
            java.lang.reflect.Parameter parameter = method.getParameters()[parameterIndex];
            Arg argAnnotation = parameter.getAnnotation(Arg.class);

            String[] tabCompletion = new String[0];
            Completion completionAnnotation = parameter.getAnnotation(Completion.class);
            if (completionAnnotation != null) {
                tabCompletion = completionAnnotation.value();
            }

            boolean wildcard = parameter.getAnnotation(Wildcard.class) != null;

            Usage usageAnnotation = parameter.getAnnotation(Usage.class);
            String usage = usageAnnotation != null ? usageAnnotation.value() : null;

            final ArgMeta argMeta;
            if (argAnnotation != null) {
                if (wildcard && parameterIndex != parametersClasses.length - 1) {
                    throw new IllegalArgumentException("Argument " + argAnnotation.value() + " is wildcard while not being last arg.");
                }
                String name = argAnnotation.value();
                if (name.isEmpty()) {
                    name = parameter.getName();
                }
                if (usage == null) {
                    usage = (!argAnnotation.defaultValue().isEmpty() ? "[" : "<") +
                            name +
                            (!argAnnotation.defaultValue().isEmpty() ? "]" : ">");
                }
                argMeta = new ArgMeta(name, wildcard, argAnnotation.defaultValue(), usage, tabCompletion, parameter.getType());
            } else {
                if (usage == null) {
                    usage = "<" + parameter.getName() + ">";
                }
                argMeta = new ArgMeta(parameter.getName(), wildcard, "", usage, tabCompletion, parameter.getType());
            }

            this.arguments.add(argMeta);
            if (argMeta.getDefaultValue() == null || argMeta.getDefaultValue().isEmpty()) {
                this.requireInputParameterCount += skippedCount + 1;
                skippedCount = 0;
            } else {
                skippedCount++;
            }

            if (argMeta.isWildcard()) {
                maxParameterCount = Integer.MAX_VALUE;
            } else {
                maxParameterCount++;
            }
        }

        Usage usage = method.getAnnotation(Usage.class);
        if (usage != null) {
            this.usage = usage.value();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < this.arguments.size(); i++) {
                stringBuilder.append("<").append(i).append(">");
                if (i + 1 < this.arguments.size()) {
                    stringBuilder.append(" ");
                }
            }
            this.usage = "<baseCommand> " + stringBuilder;
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
        return this.arguments.size();
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
        List<RV> replaceValues = new ArrayList<>();
        replaceValues.add(RV.o("<baseCommand>", this.baseCommand.getUsage() + " " + aliasUsed.toLowerCase()));

        for (int i = 0; i < this.arguments.size(); i++) {
            final ArgMeta argMeta = this.arguments.get(i);
            replaceValues.add(RV.o("<" + i + ">", argMeta.getUsage()));
        }

        return StringUtil.replace(this.usage, replaceValues.toArray(new RV[0]));
    }

    @Override
    public SubCommandType getSubCommandType() {
        return SubCommandType.METHOD_LEVEL;
    }

    @Override
    public int getMaxParameterCount() {
        return this.maxParameterCount;
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

        for (int i = 0; i < this.getArguments().size(); i++) {
            ArgMeta parameter = getArguments().get(i);
            String passedParameter = (i < arguments.length ? arguments[i] : parameter.getDefaultValue()).trim();
            if (i >= arguments.length &&
                    (parameter.getDefaultValue() == null || parameter.getDefaultValue().isEmpty())) {
                commandContext.sendMessage(MessageType.INFO, this.getUsage());
                return;
            }
            if (parameter.isWildcard() && !passedParameter.trim().equals(parameter.getDefaultValue().trim())) {
                passedParameter = toString(arguments, i);
            }
            Object result;
            try {
                result = CommandService.INSTANCE.transformParameter(commandContext, passedParameter, parameter.getParameterClass());
            } catch (ArgTransformException ex) {
                this.baseCommand.onArgumentFailed(commandContext, passedParameter, ex.getMessage());
                return;
            }
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

        if (!this.arguments.get(this.arguments.size() - 1).isWildcard() && args.length > this.getParameterCount()) {
            return Collections.emptyList();
        }

        final ArgMeta argMeta = this.arguments.get(args.length - 1);
        if (argMeta.getTabCompletion().length > 0) {
            List<String> list = new ArrayList<>();

            for (String completion : argMeta.getTabCompletion()) {
                if (completion.startsWith("@")) {
                    ArgCompletionHolder completionHolder = this.baseCommand.getTabCompletionHolder(completion.substring(1));
                    if (completionHolder != null) {
                        list.addAll(completionHolder.apply(commandContext));
                    }
                } else {
                    list.add(completion);
                }
            }

            return CoreCommandUtil.filterTabComplete(args[args.length - 1], list);
        }
        List<String> tabCompletes = COMMAND_SERVICE.tabCompleteParameters(commandContext, args[args.length - 1], argMeta.getParameterClass());
        return CoreCommandUtil.filterTabComplete(args[args.length - 1], tabCompletes);
    }

    public static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int arg = start; arg < args.length; arg++) {
            stringBuilder.append(args[arg]).append(" ");
        }

        return (stringBuilder.toString().trim());
    }

}
