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

import com.google.common.collect.ImmutableSet;
import io.fairyproject.bean.*;
import io.fairyproject.command.annotation.CommandPresence;
import io.fairyproject.command.parameter.ParameterHolder;
import io.fairyproject.module.Modular;
import io.fairyproject.util.PreProcessBatch;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO:
 * Better command
 */
@Modular(value = "core-command", abstraction = true)
@Service(name = "command")
@Getter
public class CommandService {

    public static CommandService INSTANCE;

    private Map<Class<?>, ParameterHolder<?>> parameters;
    private Map<Class<?>, PresenceProvider<?>> presenceProvidersByHolder;
    private List<CommandListener> listeners;
    private Map<String, BaseCommand> commands;

    private Map<Class<?>, PresenceProvider<?>> defaultPresenceProviders;

    private PreProcessBatch batch;

    @PreInitialize
    public void preInit() {
        this.batch = PreProcessBatch.create();
        this.parameters = new HashMap<>();
        this.presenceProvidersByHolder = new ConcurrentHashMap<>();
        this.defaultPresenceProviders = new ConcurrentHashMap<>();

        this.listeners = new ArrayList<>();
        this.commands = new ConcurrentHashMap<>();

        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(CommandListener.class)
                .onEnable(instance -> this.listeners.add((CommandListener) instance))
                .onDisable(instance -> this.listeners.remove((CommandListener) instance))
                .build());
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(BaseCommand.class)
                .onEnable(instance -> {
                    this.batch.runOrQueue(() -> this.registerCommand(instance));
                })
                .build()
        );
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(ParameterHolder.class)
                .onEnable(instance -> this.registerParameterHolder((ParameterHolder<?>) instance))
                .onDisable(instance -> this.unregisterParameterHolder((ParameterHolder<?>) instance))
                .build());
    }

    @PostInitialize
    public void init() {
        INSTANCE = this;
        this.batch.flushQueue();
    }

    public void registerDefaultPresenceProvider(PresenceProvider<?> presenceProvider) {
        this.defaultPresenceProviders.put(presenceProvider.type(), presenceProvider);
    }

    public void registerParameterHolder(ParameterHolder<?> parameterHolder) {
        for (Class<?> type : parameterHolder.type()) {
            this.parameters.put(type, parameterHolder);
        }
    }

    public void unregisterParameterHolder(ParameterHolder<?> parameterHolder) {
        for (Class<?> type : parameterHolder.type()) {
            this.parameters.remove(type, parameterHolder);
        }
    }

    @SneakyThrows
    public PresenceProvider<?> getPresenceProviderByAnnotation(CommandPresence annotation) {
        Class<? extends PresenceProvider<?>> type = annotation.value();
        if (this.presenceProvidersByHolder.containsKey(type)) {
            return this.presenceProvidersByHolder.get(type);
        }

        PresenceProvider<?> presenceProvider = type.getDeclaredConstructor().newInstance();
        this.presenceProvidersByHolder.put(type, presenceProvider);
        return presenceProvider;
    }

    @Nullable
    public PresenceProvider<?> getPresenceProviderByType(Class<?> type) {
        return this.defaultPresenceProviders.getOrDefault(type, null);
    }

    public void registerCommand(Object object) {
        BaseCommand command = (BaseCommand) object;
        command.init();

        final String[] commandNames = command.getCommandNames();
        for (String name : commandNames) {
            this.commands.put(name, command);
        }

        for (CommandListener listener : this.listeners) {
            listener.onCommandInitial(command, commandNames);
        }
    }

    public Object transformParameter(CommandContext event, String parameter, Class type) {
        if (type == String.class) {
            return parameter;
        }

        ParameterHolder<?> holder = this.parameters.getOrDefault(type, null);
        if (holder == null) {
            return null;
        }

        if (type.isEnum()) {
            try {
                return Enum.valueOf(type, parameter);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return holder.transform(event, parameter);
    }

    public List<String> tabCompleteParameters(CommandContext commandContext, String parameter, Class<?> transformTo, String[] tabCompleteFlags) {
        if (!this.parameters.containsKey(transformTo)) {
            return new ArrayList<>();
        }

        return this.parameters.get(transformTo).tabComplete(commandContext, ImmutableSet.copyOf(tabCompleteFlags), parameter);
    }

    // Should without the prefix like / or !
//    public boolean evalCommand(CommandContext commandContext) {
//        String command = commandContext.getCommand();
//
//        if (command == null || command.length() == 0) {
//            return false;
//        }
//
//        CommandMeta commandMeta = null;
//        String[] arguments = new String[0];
//
//        search:
//        for (CommandMeta meta : this.commands) {
//            for (String alias : meta.getNames()) {
//                String message = command.toLowerCase() + " ";
//                String alia = alias.toLowerCase() + " ";
//
//                if (message.startsWith(alia)) {
//                    commandMeta = meta;
//
//                    if (message.length() > alia.length()) {
//                        if (commandMeta.getParameters().size() == 0) {
//                            continue;
//                        }
//                    }
//
//                    if (command.length() > alias.length() + 1) {
//                        arguments = command.substring(alias.length() + 1).split(" ");
//                    }
//
//                    break search;
//                }
//            }
//        }
//
//        if (commandMeta == null) {
//            return false;
//        }
//
//        commandContext.setPresenceProvider(commandMeta.getPresenceProvider());
//
//        if (!commandMeta.canAccess(commandContext)) {
//            commandContext.sendNoPermission();
//            return true; // Whiizyyy - Have to be true.
//        }
//
//        if (!commandContext.shouldExecute(commandMeta, arguments)) {
//            return false;
//        }
//
//        try {
//            commandMeta.execute(commandContext, arguments);
//        } catch (Throwable throwable) {
//            commandContext.sendError(throwable);
//            Stacktrace.print(throwable);
//            return false;
//        }
//        return true;
//    }
}
