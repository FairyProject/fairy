package io.fairyproject.command.example;

import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.annotation.Arg;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.command.argument.ArgProperty;

@Command("example")
public class ExampleCommand extends BaseCommand {

    @Arg(value = "players")
    private final ArgProperty<String> players = ArgProperty.create("players", String.class);

}
