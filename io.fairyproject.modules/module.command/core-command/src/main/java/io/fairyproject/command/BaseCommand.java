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
import io.fairyproject.command.annotation.Command;
import io.fairyproject.command.argument.ArgCompletionHolder;
import io.fairyproject.command.argument.ArgProperty;
import io.fairyproject.command.exception.ArgTransformException;
import io.fairyproject.command.util.CoreCommandUtil;
import io.fairyproject.container.Autowired;
import io.fairyproject.metadata.MetadataMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;

public abstract class BaseCommand implements ICommand {

    @Autowired
    private static CommandService COMMAND_SERVICE;
    protected static final Logger LOGGER = LogManager.getLogger(BaseCommand.class);

    protected final HashMultimap<String, ICommand> subCommands = HashMultimap.create();
    protected final List<ICommand> sortedCommands = new ArrayList<>();
    protected ICommand noArgCommand;
    protected Map<String, ArgCompletionHolder> tabCompletion;

    @Getter
    protected MetadataMap metadata;
    protected PresenceProvider<?> presenceProvider;

    @Nullable
    protected BaseCommand parentCommand;
    @Getter
    protected ArgProperty<?>[] baseArgs;
    protected String[] names;
    protected String permission;

    @Getter
    protected int maxParameterCount;
    @Getter
    protected int requireInputParameterCount;

    @Getter
    protected boolean displayOnPermission;
    protected String usage;

    protected int order;

    @Override
    public int order() {
        return this.order;
    }

    public String getDescription() {
        return "";
    }

    public String[] getCommandNames() {
        return this.names;
    }

    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return this.getClass().getDeclaredAnnotation(type);
    }

    /**
     * Method called when the argument at a specific index fails to match the type
     * specified by the parameter at said index
     * @param commandContext Context of the command
     * @param source Parameter inputted
     * @param reason Exception message output by the argument parser
     */
    public void onArgumentFailed(CommandContext commandContext, String source, String reason) {
        commandContext.sendMessage(MessageType.ERROR, reason);
    }

    /**
     * Method called when the arguments are lesser than the size of the mandatory
     * parameters.
     * @param commandContext Context of the command
     * @param usage Usage of the command sent back to the executor
     */
    public void onArgumentMissing(CommandContext commandContext, String usage) {
        commandContext.sendMessage(MessageType.WARN, "Usage: " + usage);
    }

    /**
     * In the context in which none of the sub-commands match, this method will be
     * called if all arguments match the parameters.
     * @param commandContext Context of the command
     */
    public void onHelp(CommandContext commandContext) {
        List<String> messages = new ArrayList<>();
        for (ICommand command : this.sortedCommands) {
            if (!command.isDisplayOnPermission() || command.canAccess(commandContext)) {
                messages.add(command.getUsage(commandContext));
            }
        }
        commandContext.sendMessage(MessageType.INFO, messages);
    }

    /**
     * In the case of an internal exception, an error message will be output back to
     * the executor.
     * @param commandContext Context of the command
     * @param throwable Error message output back to the executor
     */
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
        this.init(this.getClass().getAnnotation(Command.class));
    }

    public void init(Command command) {
        new BaseCommandInitializer(this).init(command);
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
        if (args.length == 0) {
            if (this.noArgCommand != null) {
                return new PossibleSearches(Collections.singleton(this.noArgCommand), args, "");
            }
        } else {
            for (int i = args.length; i >= 0; i--) {
                String subcommand = StringUtils.join(args, " ", 0, i).toLowerCase();
                Set<ICommand> commands = subCommands.get(subcommand);

                if (!commands.isEmpty()) {
                    return new PossibleSearches(commands, CoreCommandUtil.arrayFromRange(args, i, args.length - 1), subcommand);
                }
            }
        }
        if (this.noArgCommand != null) {
            return new PossibleSearches(Collections.singleton(this.noArgCommand), args, "");
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

    @Override
    public String getUsage(CommandContext commandContext) {
        String baseCommand = (this.parentCommand != null ? this.parentCommand.getUsage(commandContext) : commandContext.getCommandPrefix()) + this.getCommandNames()[0];
        return this.usage.replaceAll("<baseCommand>", baseCommand);
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
