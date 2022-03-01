package io.fairyproject.command;

import io.fairyproject.command.annotation.*;
import io.fairyproject.command.argument.ArgCompletionHolder;
import io.fairyproject.command.argument.ArgProperty;
import io.fairyproject.command.completion.ArgCompletionHolderList;
import io.fairyproject.command.completion.ArgCompletionHolderStringArray;
import io.fairyproject.container.Autowired;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.reflect.Reflect;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@RequiredArgsConstructor
public class BaseCommandInitializer {

    @Autowired
    private static CommandService COMMAND_SERVICE;

    protected final BaseCommand baseCommand;

    public void init(Command command) {
        if (command != null) {
            baseCommand.names = command.value();
            baseCommand.permission = command.permissionNode();
        } else {
            throw new IllegalArgumentException("Command annotation wasn't found in class " + baseCommand.getClass());
        }

        baseCommand.metadata = MetadataMap.create();
        baseCommand.tabCompletion = new HashMap<>();

        Order order = baseCommand.getAnnotation(Order.class);
        if (order != null)
            baseCommand.order = order.value();

        PresenceProvider<?> presenceProvider = null;
        CommandPresence annotation = baseCommand.getClass().getAnnotation(CommandPresence.class);
        if (annotation != null) {
            presenceProvider = COMMAND_SERVICE.getPresenceProviderByAnnotation(annotation);
        }
        baseCommand.presenceProvider = presenceProvider;

        this.initialiseMethods();

        for (Class<?> innerClasses : baseCommand.getClass().getDeclaredClasses()) {
            if (innerClasses.isAnnotationPresent(Command.class)) {
                this.initialiseSubCommand(innerClasses, null);
            }
        }

        baseCommand.baseArgs = this.initialiseFields().toArray(new ArgProperty[0]);
        Usage usageAnnotation = baseCommand.getClass().getAnnotation(Usage.class);
        baseCommand.displayOnPermission = usageAnnotation != null && usageAnnotation.displayOnPermission();
        if (usageAnnotation != null && usageAnnotation.overwrite()) {
            baseCommand.usage = usageAnnotation.value();
        } else {
            StringJoiner stringJoiner = new StringJoiner(" ");
            for (ArgProperty<?> arg : baseCommand.baseArgs) {
                stringJoiner.add("<" + arg.getKey() + ">");
            }
            String usage = "<baseCommand>" + (stringJoiner.length() > 0 ? " " : "") + stringJoiner + " ";
            if (usageAnnotation != null) {
                usage += "- " + usageAnnotation.value();
            }
            baseCommand.usage = usage;
        }

        baseCommand.sortedCommands.addAll(baseCommand.subCommands.values());
        baseCommand.sortedCommands.sort(Comparator.comparingInt(ICommand::order));
    }

    private void initialiseMethods() {
        Set<Method> methods = new HashSet<>();
        methods.addAll(Arrays.asList(baseCommand.getClass().getMethods()));
        methods.addAll(Arrays.asList(baseCommand.getClass().getDeclaredMethods()));

        for (Method method : methods) {
            this.tryInitialiseCommandMethod(method);
            this.tryInitialiseCompletionHolderMethod(method);
        }
    }

    private void tryInitialiseCommandMethod(Method method) {
        Command command = method.getAnnotation(Command.class);

        if (command != null) {
            try {
                final CommandMeta commandMeta = new CommandMeta(command, method, baseCommand);
                for (String name : command.value()) {
                    baseCommand.subCommands.put(name.toLowerCase(), commandMeta);
                }

                baseCommand.maxParameterCount = Math.max(commandMeta.getMaxParameterCount(), baseCommand.maxParameterCount);
                baseCommand.requireInputParameterCount = Math.max(commandMeta.getRequireInputParameterCount(), baseCommand.requireInputParameterCount);
            } catch (IllegalAccessException e) {
                BaseCommand.LOGGER.error("an error got thrown while registering @Command method", e);
            }
        }
    }

    private void tryInitialiseCompletionHolderMethod(Method method) {
        CompletionHolder completion = method.getAnnotation(CompletionHolder.class);
        if (completion != null) {
            boolean hasParameter = false;
            if (method.getParameterCount() > 0) {
                hasParameter = true;
                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    BaseCommand.LOGGER.error("The parameter of @TabCompletion method should be CommandContext.", new UnsupportedOperationException());
                    return;
                }
            }

            try {
                final MethodHandle methodHandle = Reflect.lookup().unreflect(method);
                ArgCompletionHolder completionHolder;
                if (String[].class.isAssignableFrom(method.getReturnType())) {
                    completionHolder = new ArgCompletionHolderStringArray(
                            methodHandle,
                            baseCommand,
                            hasParameter,
                            completion.value()
                    );
                } else if (List.class.isAssignableFrom(method.getReturnType())) {
                    completionHolder = new ArgCompletionHolderList(
                            methodHandle,
                            baseCommand,
                            hasParameter,
                            completion.value()
                    );
                } else {
                    BaseCommand.LOGGER.error("The return type of @TabCompletion method should be String[] or List<String>", new UnsupportedOperationException());
                    return;
                }
                baseCommand.tabCompletion.put(completion.value(), completionHolder);
            } catch (IllegalAccessException e) {
                BaseCommand.LOGGER.error("an error got thrown while registering @TabCompletion method", e);
            }
        }
    }

    private List<ArgProperty<?>> initialiseFields() {
        Set<Field> fields = new HashSet<>();
        fields.addAll(Arrays.asList(baseCommand.getClass().getFields()));
        fields.addAll(Arrays.asList(baseCommand.getClass().getDeclaredFields()));

        List<ArgProperty<?>> argProperties = new ArrayList<>();
        for (Field field : fields) {
            this.initialiseArg(field, argProperties);
        }

        return argProperties;
    }

    private void initialiseArg(Field field, List<ArgProperty<?>> argProperties) {
        field.setAccessible(true);

        Command command = field.getAnnotation(Command.class);
        if (command != null) {
            if (BaseCommand.class.isAssignableFrom(field.getType())) {
                this.initialiseSubCommand(field.getType(), command);
            } else {
                throw new IllegalArgumentException("Field " + field + " marked @Command but not using type " + BaseCommand.class);
            }
        }

        Arg arg = field.getAnnotation(Arg.class);
        if (arg != null) {
            if (ArgProperty.class.isAssignableFrom(field.getType())) {
                try {
                    ArgProperty<?> property = (ArgProperty<?>) field.get(baseCommand);

                    argProperties.add(property);
                    if (property.getMissingArgument() == null) {
                        property.onMissingArgument(commandContext -> baseCommand.onArgumentMissing(commandContext, baseCommand.getUsage(commandContext)));
                    }

                    if (property.getUnknownArgument() == null) {
                        property.onUnknownArgument(baseCommand::onArgumentFailed);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("An exception got thrown while registering field arg " + field, e);
                }
            } else {
                throw new IllegalArgumentException("Field " + field + " marked @Arg but not using type " + ArgProperty.class);
            }
        }
    }

    private void initialiseSubCommand(Class<?> clazz, @Nullable Command command) {
        BaseCommand subCommand;
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            subCommand = (BaseCommand) constructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException("An exception got thrown while creating instance for " + clazz.getName() + " (Does it has no arg constructor?)", e);
        }

        subCommand.parentCommand = baseCommand;
        if (command != null) {
            subCommand.init(command);
        } else {
            subCommand.init();
        }

        for (String commandName : subCommand.getCommandNames()) {
            baseCommand.subCommands.put(commandName.toLowerCase(), subCommand);
        }
        baseCommand.maxParameterCount = Math.max(subCommand.getMaxParameterCount(), baseCommand.maxParameterCount);
        baseCommand.requireInputParameterCount = Math.max(subCommand.getRequireInputParameterCount(), baseCommand.requireInputParameterCount);
    }

}
