package io.fairyproject.discord.command;

import io.fairyproject.discord.DCBot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bot {

    Class<? extends DCBot>[] value();

}
