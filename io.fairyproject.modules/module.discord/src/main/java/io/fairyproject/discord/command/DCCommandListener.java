package io.fairyproject.discord.command;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.CommandListener;
import io.fairyproject.command.CommandService;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.discord.DCBot;
import io.fairyproject.discord.channel.DCMessageChannel;
import io.fairyproject.discord.event.DCBotInitializedEvent;
import io.fairyproject.discord.event.DCMessageReceivedEvent;
import io.fairyproject.event.Subscribe;
import io.fairyproject.metadata.MetadataKey;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@InjectableComponent
public class DCCommandListener implements CommandListener {

    private final MetadataKey<DCCommandMap> METADATA = MetadataKey.create("discord:command-map", DCCommandMap.class);

    @Autowired
    private ContainerContext containerContext;

    @Autowired
    private CommandService commandService;

    @PostInitialize
    private void onPostInitialize() {
        this.commandService.registerDefaultPresenceProvider(new DCPresenceProvider());
    }

    @Override
    public void onCommandInitial(BaseCommand command, String[] alias) {
        for (DCBot bot : this.bots(command)) {
            final DCCommandMap commandMap = bot.metadata().getOrPut(METADATA, () -> new DCCommandMap(bot));
            commandMap.register(command);
        }
    }

    @Override
    public void onCommandRemoval(BaseCommand command) {
        for (DCBot bot : this.bots(command)) {
            bot.metadata().get(METADATA).ifPresent(commandMap -> commandMap.unregister(command));
        }
    }

    @Subscribe
    public void onMessageReceived(DCMessageReceivedEvent event) {
        final DCBot bot = event.getBot();
        final User author = event.getAuthor();
        final DCMessageChannel channel = new DCMessageChannel(event.getChannel(), bot);

        bot.metadata().ifPresent(METADATA, commandMap -> {
            final String message = event.getMessage().getContentRaw();
            final String[] parts = message.split(" ");

            final DCCommandMap.CommandHolder commandHolder = commandMap.findCommand(parts[0]);
            if (commandHolder != null) {
                DCCommandContext commandContext = new DCCommandContext(Arrays.copyOfRange(parts, 1, parts.length), bot, channel, author);
                commandContext.setCommandPrefix(commandHolder.getCommandPrefix());
                commandHolder.getCommand().execute(commandContext);
            }
        });
    }

    @Subscribe
    public void onBotInitialized(DCBotInitializedEvent event) {
        final DCBot bot = event.getBot();

        for (BaseCommand command : this.commandService.getCommands().values()) {
            if (this.bots(command).contains(bot)) { // TODO - Better performance implementation?
                final DCCommandMap commandMap = bot.metadata().getOrPut(METADATA, () -> new DCCommandMap(bot));
                commandMap.register(command);
            }
        }
    }

    private Collection<DCBot> bots(BaseCommand command) {
        Collection<DCBot> bots;
        final Bot annotation = command.getAnnotation(Bot.class);
        if (annotation != null) {
            bots = new ArrayList<>();
            for (Class<? extends DCBot> botClass : annotation.value()) {
                final Object bot = this.containerContext.getContainerObject(botClass);
                if (bot != null) {
                    bots.add(( DCBot ) bot);
                } else {
                    throw new IllegalArgumentException("Couldn't find container instance for " + botClass.getName());
                }
            }
        } else {
            bots = DCBot.all();
        }
        return bots;
    }

}
