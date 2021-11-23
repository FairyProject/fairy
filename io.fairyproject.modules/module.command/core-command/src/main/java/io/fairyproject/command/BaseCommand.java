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

import com.google.common.collect.HashMultimap;
import io.fairyproject.bean.Autowired;
import io.fairyproject.command.annotation.Arg;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.command.annotation.CommandPresence;
import io.fairyproject.command.annotation.CompletionHolder;
import io.fairyproject.command.argument.ArgCompletionHolder;
import io.fairyproject.command.argument.ArgProperty;
import io.fairyproject.command.exception.ArgTransformException;
import io.fairyproject.command.util.CoreCommandUtil;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.reflect.Reflect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class BaseCommand implements ICommand {

    @Autowired
    private static CommandService COMMAND_SERVICE;
    private static final Logger LOGGER = LogManager.getLogger(BaseCommand.class);

    private final HashMultimap<String, ICommand> subCommands = HashMultimap.create();
    private Map<String, ArgCompletionHolder> tabCompletion;

    @Getter
    private MetadataMap metadata;
    private PresenceProvider<?> presenceProvider;

    @Nullable
    private BaseCommand parentCommand;
    @Getter
    private ArgProperty<?>[] baseArgs;
    private String[] names;
    private String permission;

    @Getter
    private int maxParameterCount;
    @Getter
    private int requireInputParameterCount;

    public String getDescription() {
        return "";
    }

    public String[] getCommandNames() {
        return this.names;
    }

    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return this.getClass().getAnnotation(type);
    }

    public void onArgumentFailed(CommandContext commandContext, String source, String reason) {
        commandContext.sendMessage(MessageType.ERROR, reason);
    }

    public void onArgumentMissing(CommandContext commandContext, String usage) {
        commandContext.sendMessage(MessageType.WARN, "Usage: " + usage);
    }

    public void onHelp(CommandContext commandContext) {
        List<String> messages = new ArrayList<>();
        for (ICommand command : this.subCommands.values()) {
            switch (command.getSubCommandType()) {
                case CLASS_LEVEL:
                    messages.add(command.getUsage() + " ...");
                    break;
                case METHOD_LEVEL:
                    messages.add(command.getUsage());
                    break;
            }
        }
        commandContext.sendMessage(MessageType.INFO, messages);
    }

    public void onError(CommandContext commandContext, Throwable throwable) {
        commandContext.sendMessage(MessageType.ERROR, "Internal Exception: " + throwable.getClass().getName() + " - " + throwable.getMessage());
    }

    @Override
    public boolean canAccess(CommandContext commandContext) {
        if (this.permission == null || this.permission.length() == 0) {
            return true;
        }

        return commandContext.hasPermission(this.permission);
    }

    public void init() {
        Command command = this.getClass().getAnnotation(Command.class);
        if (command != null) {
            this.names = command.value();
            this.permission = command.permissionNode();
        } else {
            throw new IllegalArgumentException("Command annotation wasn't found in class " + this.getClass());
        }

        this.metadata = MetadataMap.create();
        this.tabCompletion = new HashMap<>();

        PresenceProvider<?> presenceProvider = null;
        CommandPresence annotation = this.getClass().getAnnotation(CommandPresence.class);
        if (annotation != null) {
            presenceProvider = COMMAND_SERVICE.getPresenceProviderByAnnotation(annotation);
        }
        this.presenceProvider = presenceProvider;

        Set<Method> methods = new HashSet<>();
        methods.addAll(Arrays.asList(this.getClass().getMethods()));
        methods.addAll(Arrays.asList(this.getClass().getDeclaredMethods()));

        for (Method method : methods) {
            command = method.getAnnotation(Command.class);

            if (command != null) {
                try {
                    final CommandMeta commandMeta = new CommandMeta(command, method, this);
                    for (String name : command.value()) {
                        this.subCommands.put(name.toLowerCase(), commandMeta);
                    }

                    this.maxParameterCount = Math.max(commandMeta.getMaxParameterCount(), maxParameterCount);
                    this.requireInputParameterCount = Math.max(commandMeta.getRequireInputParameterCount(), requireInputParameterCount);
                } catch (IllegalAccessException e) {
                    LOGGER.error("an error got thrown while registering @Command method", e);
                }
            }

            CompletionHolder completion = method.getAnnotation(CompletionHolder.class);
            if (completion != null) {
                boolean hasParameter = false;
                if (method.getParameterCount() > 0) {
                    hasParameter = true;
                    if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        LOGGER.error("The parameter of @TabCompletion method should be CommandContext.", new UnsupportedOperationException());
                        continue;
                    }
                }

                try {
                    final MethodHandle methodHandle = Reflect.lookup().unreflect(method);
                    ArgCompletionHolder completionHolder;
                    if (String[].class.isAssignableFrom(method.getReturnType())) {
                        boolean finalHasParameter = hasParameter;
                        completionHolder = new ArgCompletionHolder() {
                            @Override
                            public Collection<String> apply(CommandContext commandContext) {
                                try {
                                    if (finalHasParameter) {
                                        return Arrays.asList((String[]) methodHandle.invoke(BaseCommand.this, commandContext));
                                    } else {
                                        return Arrays.asList((String[]) methodHandle.invoke(BaseCommand.this));
                                    }
                                } catch (Throwable e) {
                                    throw new IllegalArgumentException(e);
                                }
                            }

                            @Override
                            public String name() {
                                return completion.value();
                            }
                        };
                    } else if (List.class.isAssignableFrom(method.getReturnType())) {
                        boolean finalHasParameter = hasParameter;
                        completionHolder = new ArgCompletionHolder() {
                            @Override
                            public Collection<String> apply(CommandContext commandContext) {
                                try {
                                    if (finalHasParameter) {
                                        return (List<String>) methodHandle.invoke(BaseCommand.this, commandContext);
                                    } else {
                                        return (List<String>) methodHandle.invoke(BaseCommand.this);
                                    }
                                } catch (Throwable e) {
                                    throw new IllegalArgumentException(e);
                                }
                            }

                            @Override
                            public String name() {
                                return completion.value();
                            }
                        };
                    } else {
                        LOGGER.error("The return type of @TabCompletion method should be String[] or List<String>", new UnsupportedOperationException());
                        continue;
                    }
                    this.tabCompletion.put(completion.value(), completionHolder);
                } catch (IllegalAccessException e) {
                    LOGGER.error("an error got thrown while registering @TabCompletion method", e);
                }
            }
        }

        for (Class<?> innerClasses : this.getClass().getDeclaredClasses()) {
            if (innerClasses.isAnnotationPresent(Command.class)) {
                if (!BaseCommand.class.isAssignableFrom(innerClasses)) {
                    throw new IllegalArgumentException("The class " + this.getClass() + " was annotated as @Command but wasn't extending BaseCommand.");
                }

                BaseCommand subCommand;
                try {
                    subCommand = (BaseCommand) innerClasses.getDeclaredConstructor().newInstance();
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new IllegalArgumentException("An exception got thrown while creating instance for " + innerClasses + " (Does it has no arg constructor?");
                }

                subCommand.parentCommand = this;
                subCommand.init();

                for (String commandName : subCommand.getCommandNames()) {
                    this.subCommands.put(commandName.toLowerCase(), subCommand);
                }
                this.maxParameterCount = Math.max(subCommand.getMaxParameterCount(), maxParameterCount);
                this.requireInputParameterCount = Math.max(subCommand.getRequireInputParameterCount(), requireInputParameterCount);
            }
        }

        Set<Field> fields = new HashSet<>();
        fields.addAll(Arrays.asList(this.getClass().getFields()));
        fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));

        List<ArgProperty<?>> argProperties = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            Arg arg = field.getAnnotation(Arg.class);
            if (arg != null) {
                if (!ArgProperty.class.isAssignableFrom(field.getType())) {
                    throw new IllegalArgumentException("Field " + field + " marked @Arg but not using type " + ArgProperty.class);
                }

                try {
                    ArgProperty<?> property = (ArgProperty<?>) field.get(this);

                    argProperties.add(property);
                    if (property.getMissingArgument() == null) {
                        property.onMissingArgument(commandContext -> this.onArgumentMissing(commandContext, this.getUsage()));
                    }

                    if (property.getUnknownArgument() == null) {
                        property.onUnknownArgument(this::onArgumentFailed);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("An exception got thrown while registering field arg " + field, e);
                }
            }
        }

        this.baseArgs = argProperties.toArray(new ArgProperty[0]);
    }

    @Override
    public void execute(CommandContext commandContext) {
        if (this.presenceProvider != null) {
            commandContext.setPresenceProvider(this.presenceProvider);
        } else {
            commandContext.setPresenceProvider(COMMAND_SERVICE.getPresenceProviderByType(commandContext.getClass()));
        }

        if (!this.resolveBaseArguments(commandContext)) {
            return;
        }

        final Pair<ICommand, String[]> pair = this.findSubCommand(commandContext, false);
        if (pair == null) {
            this.onHelp(commandContext);
            return;
        }

        commandContext.setArgs(pair.getRight());
        pair.getLeft().execute(commandContext);
    }

    public boolean resolveBaseArguments(CommandContext commandContext) {
        if (this.baseArgs.length == 0) {
            return true;
        }

        final String[] args = commandContext.getArgs();
        if (args.length < this.baseArgs.length) {
            final ArgProperty<?> baseArg = this.baseArgs[args.length];

            baseArg.getMissingArgument().accept(commandContext);
            return false;
        }

        for (int i = 0; i < this.baseArgs.length; i++) {
            Object obj;
            final ArgProperty<?> baseArg = baseArgs[i];
            try {
                if (this.baseArgs[i].getParameterHolder() != null) {
                    obj = this.baseArgs[i].getParameterHolder().transform(commandContext, args[i]);
                } else {
                    obj = CommandService.INSTANCE.transformParameter(commandContext, args[i], this.baseArgs[i].getType());
                }
            } catch (ArgTransformException ex) {
                baseArg.getUnknownArgument().accept(commandContext, args[i], ex.getMessage());
                return false;
            }

            if (obj == null) {
                baseArg.getUnknownArgument().accept(commandContext, args[i], "ArgTransformer doesn't return result.");
                return false;
            }
            try {
                commandContext.addProperty(baseArg, baseArg.cast(obj));
            } catch (Throwable throwable) {
                baseArg.getUnknownArgument().accept(commandContext, args[i], "ArgTransformer returned a type unmatched result.");
                return false;
            }
        }

        commandContext.setArgs(CoreCommandUtil.arrayFromRange(args, this.baseArgs.length, args.length - 1));
        return true;
    }

    public String getCommandPrefix() {
        if (this.parentCommand != null) {
            return this.parentCommand.getCommandPrefix() + this.getCommandNames()[0] + " ";
        }
        return "/" + this.getCommandNames()[0] + " ";
    }

    @Override
    public List<String> completeCommand(CommandContext commandContext) {
        if (this.presenceProvider != null) {
            commandContext.setPresenceProvider(this.presenceProvider);
        } else {
            commandContext.setPresenceProvider(COMMAND_SERVICE.getPresenceProviderByType(commandContext.getClass()));
        }

        final Pair<List<String>, Boolean> pair = this.completeArguments(commandContext);

        List<String> result = pair.getLeft();
        if (pair.getRight()) {
            result = new ArrayList<>(result);
            result.addAll(this.getCommandsForCompletion(commandContext));
        }

        return result;
    }

    private Pair<List<String>, Boolean> completeArguments(CommandContext commandContext) {
        if (commandContext.getArgs().length == 0) {
            return Pair.of(Collections.emptyList(), true);
        }

        final List<String> base = this.completeBaseArguments(commandContext);
        if (base != null) {
            commandContext.setArgs(CoreCommandUtil.arrayFromRange(commandContext.getArgs(), this.baseArgs.length, commandContext.getArgs().length - 1));
            return Pair.of(base, false);
        }

        final Pair<ICommand, String[]> subCommand = this.findSubCommand(commandContext, true);
        if (subCommand != null) {
            commandContext.setArgs(subCommand.getRight());
            return Pair.of(subCommand.getLeft().completeCommand(commandContext), false);
        }
        return Pair.of(Collections.emptyList(), true);
    }

    public List<String> completeBaseArguments(CommandContext commandContext) {
        if (this.baseArgs.length == 0) {
            return null;
        }

        final String[] args = commandContext.getArgs();
        if (args.length <= this.baseArgs.length) {
            final ArgProperty<?> baseArg = this.baseArgs[args.length - 1];

            final String source = args[args.length - 1];
            if (baseArg.getParameterHolder() != null) {
                return baseArg.getParameterHolder().tabComplete(commandContext, source);
            } else {
                return COMMAND_SERVICE.tabCompleteParameters(commandContext, source, baseArg.getType());
            }
        }

        commandContext.setArgs(CoreCommandUtil.arrayFromRange(args, this.baseArgs.length, args.length - 1));
        return null;
    }

    public ArgCompletionHolder getTabCompletionHolder(String name) {
        if (this.tabCompletion.containsKey(name.toLowerCase())) {
            return this.tabCompletion.get(name.toLowerCase());
        }

        if (this.parentCommand != null) {
            return this.parentCommand.getTabCompletionHolder(name.toLowerCase());
        }
        return COMMAND_SERVICE.getTabCompletionHolder(name);
    }

    public List<String> getCommandsForCompletion(CommandContext commandContext) {
        final String[] args = commandContext.getArgs();
        final Set<String> commands = new HashSet<>();
        final int cmdIndex = Math.max(0, args.length - 1);
        String argString = StringUtils.join(args, " ").toLowerCase();
        for (Map.Entry<String, ICommand> entry : subCommands.entries()) {
            final String key = entry.getKey();
            if (key.startsWith(argString)) {
                final ICommand value = entry.getValue();
                if (!value.canAccess(commandContext)) {
                    continue;
                }

                String[] split = key.split(" ");
                commands.add(split[cmdIndex]);
            }
        }
        return new ArrayList<>(commands);
    }

    private Pair<ICommand, String[]> findSubCommand(CommandContext commandContext, boolean completion) {
        final String[] args = commandContext.getArgs();
        final PossibleSearches possibleSubCommands = this.findPossibleSubCommands(commandContext, args);

        if (possibleSubCommands == null) {
            return null;
        } else if (possibleSubCommands.getPossibleCommands().size() == 1) {
            return Pair.of(getFirstElement(possibleSubCommands.getPossibleCommands()), possibleSubCommands.getArgs());
        } else {
            Optional<ICommand> optional = possibleSubCommands.getPossibleCommands().stream()
                    .filter(c -> isProbableMatch(c, args, completion))
                    .min((c1, c2) -> {
                        int a = c1.getMaxParameterCount();
                        int b = c2.getMaxParameterCount();

                        if (a == b) {
                            return 0;
                        }
                        return a < b ? 1 : -1;
                    });
            if (optional.isPresent()) {
                return Pair.of(optional.get(), possibleSubCommands.getArgs());
            }
        }

        return null;
    }

    private boolean isProbableMatch(ICommand c, String[] args, boolean completion) {
        int required = c.getRequireInputParameterCount();
        return args.length <= c.getMaxParameterCount() && (completion || args.length >= required);
    }

    private PossibleSearches findPossibleSubCommands(CommandContext commandContext, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            String subcommand = StringUtils.join(args, " ", 0, i).toLowerCase();
            Set<ICommand> commands = subCommands.get(subcommand);

            if (!commands.isEmpty()) {
                return new PossibleSearches(commands, CoreCommandUtil.arrayFromRange(args, i, args.length - 1), subcommand);
            }
        }

        return null;
    }

    private static <T> T getFirstElement(Iterable<T> iterable) {
        if (iterable == null) {
            return null;
        }
        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    public String getUsage() {
        StringJoiner stringJoiner = new StringJoiner(" ");
        for (ArgProperty<?> arg : this.baseArgs) {
            stringJoiner.add("<" + arg.getKey() + ">");
        }
        return (this.parentCommand != null ? this.parentCommand.getUsage() + " " : "/") + this.getCommandNames()[0] + " " + stringJoiner;
    }

    @Override
    public SubCommandType getSubCommandType() {
        return SubCommandType.CLASS_LEVEL;
    }

    @RequiredArgsConstructor
    @Getter
    private static class PossibleSearches {

        private final Set<ICommand> possibleCommands;
        private final String[] args;
        private final String subCommand;

    }

}
