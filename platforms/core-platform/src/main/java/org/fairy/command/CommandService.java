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

package org.fairy.command;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.SneakyThrows;
import org.fairy.bean.*;
import org.fairy.command.annotation.Command;
import org.fairy.command.annotation.CommandHolder;
import org.fairy.command.annotation.CommandPresence;
import org.fairy.command.annotation.Parameter;
import org.fairy.command.parameter.ParameterHolder;
import org.fairy.command.parameter.ParameterMeta;
import org.fairy.util.Stacktrace;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO:
 * Better command
 *
 */

@Service(name = "command")
@Getter
public class CommandService {

    public static CommandService INSTANCE;

    private Map<Class<?>, ParameterHolder> parameters;
    private Map<Class<?>, PresenceProvider> presenceProvidersByHolder;
    private List<CommandMeta> commands;

    private Map<Class<?>, PresenceProvider> defaultPresenceProviders;

    @PreInitialize
    public void preInit() {
        this.parameters = new HashMap<>();
        this.presenceProvidersByHolder = new ConcurrentHashMap<>();
        this.defaultPresenceProviders = new ConcurrentHashMap<>();

        this.commands = new ArrayList<>();

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {CommandHolder.class};
            }

            @Override
            public Object newInstance(Class<?> type) {
                Object object = super.newInstance(type);
                registerCommandHolder(object, type);
                return object;
            }
        });

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {ParameterHolder.class};
            }

            @Override
            public Object newInstance(Class<?> type) {
                Object holder = super.newInstance(type);
                registerParameterHolder((ParameterHolder) holder);

                return holder;
            }
        });
    }

    @PostInitialize
    public void init() {
        INSTANCE = this;
    }

    public void registerDefaultPresenceProvider(PresenceProvider presenceProvider) {
        this.defaultPresenceProviders.put(presenceProvider.type(), presenceProvider);
    }

    public void registerParameterHolder(ParameterHolder parameterHolder) {
        for (Class type : parameterHolder.type()) {
            this.parameters.put(type, parameterHolder);
        }
    }

    @SneakyThrows
    public PresenceProvider getPresenceProviderByAnnotation(CommandPresence annotation) {
        Class<? extends PresenceProvider> type = annotation.value();
        if (this.presenceProvidersByHolder.containsKey(type)) {
            return this.presenceProvidersByHolder.get(type);
        }

        PresenceProvider presenceProvider = type.newInstance();
        this.presenceProvidersByHolder.put(type, presenceProvider);
        return presenceProvider;
    }

    @Nullable
    public PresenceProvider getPresenceProviderByType(Class<?> type) {
        return this.defaultPresenceProviders.getOrDefault(type, null);
    }

    public void registerCommandHolder(Object holder, Class<?> type) {
        PresenceProvider<?> presenceProvider = null;
        CommandPresence annotation = type.getAnnotation(CommandPresence.class);
        if (annotation != null) {
            presenceProvider = this.getPresenceProviderByAnnotation(annotation);
        }

        for (Method method : holder.getClass().getDeclaredMethods()) {
            Command command = method.getAnnotation(Command.class);
            if (command != null) {

                final List<ParameterMeta> parameterData = new ArrayList<>();
                Class<?>[] parameters = method.getParameterTypes();

                // Offset of 1 here for the sender parameter.
                for (int parameterIndex = 1; parameterIndex < parameters.length; parameterIndex++) {
                    java.lang.reflect.Parameter parameter = method.getParameters()[parameterIndex];
                    Parameter parameterAnnotation = parameter.getAnnotation(Parameter.class);

                    if (parameterAnnotation != null) {
                        parameterData.add(new ParameterMeta(parameterAnnotation.name(), parameterAnnotation.wildcard(), parameterAnnotation.defaultValue(), parameterAnnotation.tabCompleteFlags(), parameter.getType()));
                    } else {
                        parameterData.add(new ParameterMeta(parameter.getName(), false, "", new String[] {""}, parameter.getType()));
                    }
                }
                PresenceProvider<?> presenceProviderMethod = presenceProvider;
                CommandPresence annotationMethod = method.getAnnotation(CommandPresence.class);
                if (annotationMethod != null) {
                    presenceProviderMethod = this.getPresenceProviderByAnnotation(annotationMethod);
                }
                if (presenceProviderMethod == null) {
                    presenceProviderMethod = this.getPresenceProviderByType(parameters[0]);
                }

                if (presenceProviderMethod == null) {
                    throw new IllegalArgumentException("The method " + method + " with first parameters " + parameters[0].getSimpleName() + " doesn't have match presence provider!");
                }

                if (!parameters[0].isAssignableFrom(presenceProviderMethod.type())) {
                    throw new IllegalArgumentException("The method " + method + " with first parameters " + parameters[0].getSimpleName() + " doesn't match to " + presenceProviderMethod.getClass().getSimpleName() + " (requires type: " + presenceProviderMethod.type().getSimpleName() + ")");
                }

                CommandMeta meta = new CommandMeta(command.names(), command.permissionNode(), parameterData, holder, method, presenceProviderMethod);
                this.commands.add(meta);
            }
        }
    }

    public Object transformParameter(CommandEvent event, String parameter, Class type) {
        if (type == String.class) {
            return parameter;
        }

        ParameterHolder holder = this.parameters.getOrDefault(type, null);
        if (holder == null) {
            return null;
        }

        if (type.isEnum()) {
            try {
                return Enum.valueOf(type, parameter);
            } catch (IllegalArgumentException ignored) {}
        }

        return holder.transform(event, parameter);
    }

    public List<String> tabCompleteParameters(Object user, String[] parameters, String parameter, Class<?> transformTo, String[] tabCompleteFlags) {
        if (!this.parameters.containsKey(transformTo)) {
            return new ArrayList<>();
        }

        return this.parameters.get(transformTo).tabComplete(user, parameters, ImmutableSet.copyOf(tabCompleteFlags), parameter);
    }

    // Should without the prefix like / or !
    public boolean evalCommand(CommandEvent commandEvent) {
        Object user = commandEvent.getUser();
        String command = commandEvent.getCommand();

        if (command == null || command.length() == 0) {
            return false;
        }

        CommandMeta commandMeta = null;
        String[] arguments = new String[0];

        search:
        for (CommandMeta meta : this.commands) {
            for (String alias : meta.getNames()) {
                String message = command.toLowerCase() + " ";
                String alia = alias.toLowerCase() + " ";

                if (message.startsWith(alia)) {
                    commandMeta = meta;

                    if (message.length() > alia.length()) {
                        if (commandMeta.getParameters().size() == 0) {
                            continue;
                        }
                    }

                    if (command.length() > alias.length() + 1) {
                        arguments = command.substring(alias.length() + 1).split(" ");
                    }

                    break search;
                }
            }
        }

        if (commandMeta == null) {
            return false;
        }

        commandEvent.setPresenceProvider(commandMeta.getPresenceProvider());

        if (!commandMeta.canAccess(commandEvent)) {
            commandEvent.sendNoPermission();
            return true; // Whiizyyy - Have to be true.
        }

        if (!commandEvent.shouldExecute(commandMeta, arguments)) {
            return false;
        }

        try {
            commandMeta.execute(commandEvent, arguments);
        } catch (Throwable throwable) {
            commandEvent.sendError(throwable);
            Stacktrace.print(throwable);
            return false;
        }
        return true;
    }
}
