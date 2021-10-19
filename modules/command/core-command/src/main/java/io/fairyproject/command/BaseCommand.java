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
import io.fairyproject.command.annotation.Command;
import io.fairyproject.command.annotation.CommandPresence;
import io.fairyproject.command.annotation.TabCompletion;
import io.fairyproject.command.argument.ArgTabCompletionHolder;
import io.fairyproject.reflect.Reflect;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.*;

public abstract class BaseCommand {

    @Autowired
    private static CommandService COMMAND_SERVICE;
    private static final Logger LOGGER = LogManager.getLogger();

    private final HashMultimap<String, CommandMeta> subCommands = HashMultimap.create();
    private Map<String, ArgTabCompletionHolder> tabCompletion;
    private PresenceProvider<?> presenceProvider;

    public String getDescription() {
        return "";
    }

    public String[] getCommandNames() {
        Command command = this.getClass().getAnnotation(Command.class);
        if (command != null) {
            return command.value();
        }
        throw new UnsupportedOperationException();
    }

    public void init() {
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
            Command command = method.getAnnotation(Command.class);

            if (command != null) {
                try {
                    final CommandMeta commandMeta = new CommandMeta(command, method, this);
                    for (String name : command.value()) {
                        this.subCommands.put(name.toLowerCase(), commandMeta);
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("an error got thrown while registering @Command method", e);
                }
            }

            TabCompletion tabCompletion = method.getAnnotation(TabCompletion.class);
            if (tabCompletion != null) {
                if (method.getParameterCount() != 1 || CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    LOGGER.error("The parameter of @TabCompletion method should be CommandContext.", new UnsupportedOperationException());
                    continue;
                }

                try {
                    final MethodHandle methodHandle = Reflect.lookup().unreflect(method);
                    ArgTabCompletionHolder completionHolder;
                    if (String[].class.isAssignableFrom(method.getReturnType())) {
                        completionHolder = commandContext -> {
                            try {
                                return Arrays.asList((String[]) methodHandle.invoke(this, commandContext));
                            } catch (Throwable e) {
                                throw new IllegalArgumentException(e);
                            }
                        };
                    } else if (List.class.isAssignableFrom(method.getReturnType())) {
                        completionHolder = commandContext -> {
                            try {
                                return (List<String>) methodHandle.invoke(this, commandContext);
                            } catch (Throwable e) {
                                throw new IllegalArgumentException(e);
                            }
                        };
                    } else {
                        LOGGER.error("The return type of @TabCompletion method should be String[] or List<String>", new UnsupportedOperationException());
                        continue;
                    }
                    for (String name : tabCompletion.value()) {
                        this.tabCompletion.put(name.toLowerCase(), completionHolder);
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("an error got thrown while registering @TabCompletion method", e);
                }
            }
        }
    }

    public void help(CommandContext commandContext) {
        commandContext.sendMessage(MessageType.INFO, "halo!");
    }

    public void evalCommand(CommandContext commandContext) {
        final Pair<CommandMeta, String[]> pair = this.findSubCommand(commandContext, false);
        if (this.presenceProvider != null) {
            commandContext.setPresenceProvider(this.presenceProvider);
        } else {
            commandContext.setPresenceProvider(COMMAND_SERVICE.getPresenceProviderByType(commandContext.getClass()));
        }

        if (pair == null) {
            this.help(commandContext);
            return;
        }

        commandContext.setArgs(pair.getRight());
        pair.getLeft().execute(commandContext);
    }

    public List<String> completeCommand(CommandContext commandContext) {
        final Pair<CommandMeta, String[]> subCommand = this.findSubCommand(commandContext, true);
        if (subCommand != null) {
            return subCommand.getLeft().completeCommand(commandContext);
        }
        return Collections.emptyList();
    }

    public List<String> getCommandsForCompletion(CommandContext commandContext) {
        final String[] args = commandContext.getArgs();
        final Set<String> commands = new HashSet<>();
        final int cmdIndex = Math.max(0, args.length - 1);
        String argString = StringUtils.join(args, " ").toLowerCase();
        for (Map.Entry<String, CommandMeta> entry : subCommands.entries()) {
            final String key = entry.getKey();
            if (key.startsWith(argString)) {
                final CommandMeta value = entry.getValue();
                if (!value.canAccess(commandContext)) {
                    continue;
                }

                String[] split = key.split(" ");
                commands.add(split[cmdIndex]);
            }
        }
        return new ArrayList<>(commands);
    }

    private Pair<CommandMeta, String[]> findSubCommand(CommandContext commandContext, boolean completion) {
        final String[] args = commandContext.getArgs();
        final PossibleSearches possibleSubCommands = this.findPossibleSubCommands(commandContext, args);

        if (possibleSubCommands == null) {
            return null;
        } else if (possibleSubCommands.getPossibleCommands().size() == 1) {
            return Pair.of(getFirstElement(possibleSubCommands.getPossibleCommands()), possibleSubCommands.getArgs());
        } else {
            Optional<CommandMeta> optional = possibleSubCommands.getPossibleCommands().stream()
                    .filter(c -> isProbableMatch(c, args, completion))
                    .min((c1, c2) -> {
                        int a = c1.getParameterCount();
                        int b = c2.getParameterCount();

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

    private boolean isProbableMatch(CommandMeta c, String[] args, boolean completion) {
        int required = c.getRequireInputParameterCount();
        int optional = c.getOptionalInputParameterCount();
        return args.length <= required + optional && (completion || args.length >= required);
    }

    private PossibleSearches findPossibleSubCommands(CommandContext commandContext, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            String subcommand = StringUtils.join(args, " ", 0, i).toLowerCase();
            Set<CommandMeta> commands = subCommands.get(subcommand);

            if (!commands.isEmpty()) {
                return new PossibleSearches(commands, Arrays.copyOfRange(args, i, args.length), subcommand);
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

    @RequiredArgsConstructor
    @Getter
    private static class PossibleSearches {

        private final Set<CommandMeta> possibleCommands;
        private final String[] args;
        private final String subCommand;

    }

}
