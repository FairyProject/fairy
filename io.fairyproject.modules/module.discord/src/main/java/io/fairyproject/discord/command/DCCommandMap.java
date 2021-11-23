package io.fairyproject.discord.command;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.discord.DCBot;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DCCommandMap {

    @Getter
    private final DCBot bot;
    private final Map<String, BaseCommand> commands;

    public DCCommandMap(DCBot bot) {
        this.bot = bot;
        this.commands = new ConcurrentHashMap<>();
    }

    public BaseCommand findCommand(String name) {
        return this.commands.getOrDefault(name, null);
    }

    public void register(BaseCommand command) {
        for (String commandName : command.getCommandNames()) {
            this.commands.put(commandName.toLowerCase(), command);
        }
    }

    public void unregister(BaseCommand command) {
        for (String commandName : command.getCommandNames()) {
            this.commands.remove(commandName.toLowerCase());
        }
    }

}
