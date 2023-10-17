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

import io.fairyproject.command.annotation.CommandPresence;
import io.fairyproject.command.argument.ArgCompletionHolder;
import io.fairyproject.command.exception.ArgTransformException;
import io.fairyproject.command.parameter.ArgTransformer;
import io.fairyproject.container.*;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.log.Log;
import io.fairyproject.util.PreProcessBatch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO:
 * Better command
 */
@InjectableComponent
@RequiredArgsConstructor
@Getter
public class CommandService {

    public static CommandService INSTANCE;

    private final ContainerContext context;

    private Map<Class<?>, ArgTransformer<?>> parameters;
    private Map<Class<?>, PresenceProvider<?>> presenceProvidersByHolder;
    private Map<Class<?>, PresenceProvider<?>> defaultPresenceProviders;
    private Map<String, ArgCompletionHolder> tabCompletionHolders;
    private List<CommandListener> listeners;
    private Map<String, BaseCommand> commands;

    private PreProcessBatch batch;

    @PreInitialize
    public void preInit() {
        this.batch = PreProcessBatch.create();
        this.parameters = new HashMap<>();
        this.presenceProvidersByHolder = new ConcurrentHashMap<>();
        this.defaultPresenceProviders = new ConcurrentHashMap<>();
        this.tabCompletionHolders = new ConcurrentHashMap<>();

        this.listeners = new ArrayList<>();
        this.commands = new ConcurrentHashMap<>();

        this.context.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(CommandListener.class))
                .withAddHandler(ContainerObjCollector.warpInstance(CommandListener.class, this.listeners::add))
                .withRemoveHandler(ContainerObjCollector.warpInstance(CommandListener.class, this.listeners::remove))
        );
        this.context.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(ArgCompletionHolder.class))
                .withAddHandler(ContainerObjCollector.warpInstance(ArgCompletionHolder.class, handler -> this.tabCompletionHolders.put(handler.name(), handler)))
                .withRemoveHandler(ContainerObjCollector.warpInstance(ArgCompletionHolder.class, handler -> this.tabCompletionHolders.remove(handler.name())))
        );
        this.context.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(BaseCommand.class))
                .withAddHandler(ContainerObjCollector.warpInstance(BaseCommand.class, instance -> this.batch.runOrQueue(instance.getClass().getName(), () -> this.registerCommand(instance))))
                .withRemoveHandler(ContainerObjCollector.warpInstance(BaseCommand.class, instance -> {
                    if (!this.batch.remove(instance.getClass().getName())) {
                        this.unregisterCommand(instance);
                    }
                }))
        );
        this.context.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(ArgTransformer.class))
                .withAddHandler(ContainerObjCollector.warpInstance(ArgTransformer.class, this::registerArgTransformer))
                .withRemoveHandler(ContainerObjCollector.warpInstance(ArgTransformer.class, this::unregisterArgTransformer))
        );
        Log.info("Initialized command service...");
    }

    @PostInitialize
    public void init() {
        INSTANCE = this;
        Log.info("Injecting fairy commands...");
        this.batch.flushQueue();
        Log.info("Injected!");
    }

    public void registerDefaultPresenceProvider(PresenceProvider<?> presenceProvider) {
        this.defaultPresenceProviders.put(presenceProvider.type(), presenceProvider);
    }

    public void registerArgTransformer(ArgTransformer<?> parameterHolder) {
        for (Class<?> type : parameterHolder.type()) {
            this.parameters.put(type, parameterHolder);
        }
    }

    public void unregisterArgTransformer(ArgTransformer<?> parameterHolder) {
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

    public void unregisterCommand(Object object) {
        BaseCommand command = (BaseCommand) object;

        final String[] commandNames = command.getCommandNames();
        for (String name : commandNames) {
            this.commands.put(name, command);
        }

        // send remove signal to listeners
        this.listeners.forEach(listener -> listener.onCommandRemoval(command));
    }

    public Object transformParameter(CommandContext event, String source, Class type) {
        if (type == String.class) {
            return source;
        }

        ArgTransformer<?> holder = this.parameters.getOrDefault(type, null);
        if (holder != null) {
            return holder.transform(event, source);
        }

        if (type.isEnum()) {
            try {
                return Enum.valueOf(type, source);
            } catch (IllegalArgumentException ignored) {
                throw new ArgTransformException("Unmatched type: " + Stream.of(type.getEnumConstants())
                        .map(obj -> (Enum<?>) obj)
                        .map(Enum::name)
                        .collect(Collectors.joining(", "))
                );
            }
        }

        return null;
    }

    public ArgCompletionHolder getTabCompletionHolder(String name) {
        return this.tabCompletionHolders.get(name);
    }

    public List<String> tabCompleteParameters(CommandContext commandContext, String parameter, Class<?> transformTo) {
        final ArgTransformer<?> argTransformer = this.parameters.getOrDefault(transformTo, null);
        if (argTransformer == null) {
            if (transformTo.isEnum()) {
                List<String> strings = new ArrayList<>();

                for (Object constant : transformTo.getEnumConstants()) {
                    final String name = ((Enum<?>) constant).name();
                    if (name.toLowerCase().startsWith(parameter.toLowerCase())) {
                        strings.add(name);
                    }
                }

                return strings;
            }
            return Collections.emptyList();
        }

        return argTransformer.tabComplete(commandContext, parameter);
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
