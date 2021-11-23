package io.fairyproject.discord.command;

import io.fairyproject.discord.DCBot;

public @interface Bot {

    Class<? extends DCBot>[] value();

}
