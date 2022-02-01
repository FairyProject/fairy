package io.fairyproject.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;

@UtilityClass
public class Conditions {

    @Contract("false, _ -> fail")
    public static void check(final boolean expression, final String message)
    {
        if (!expression)
            throw new IllegalArgumentException(message);
    }

    @Contract("false, _, _ -> fail")
    public static void check(final boolean expression, final String message, final Object... args)
    {
        if (!expression)
            throw new IllegalArgumentException(String.format(message, args));
    }

    @Contract("false, _, _ -> fail")
    public static void check(final boolean expression, final String message, final Object arg)
    {
        if (!expression)
            throw new IllegalArgumentException(String.format(message, arg));
    }

    @Contract("null, _ -> fail")
    public static void notNull(final Object argument, final String name)
    {
        if (argument == null)
            throw new IllegalArgumentException(name + " may not be null");
    }

    @Contract("null, _ -> fail")
    public static void notEmpty(final CharSequence argument, final String name)
    {
        notNull(argument, name);
        if (argument.length() == 0)
            throw new IllegalArgumentException(name + " may not be empty");
    }

}
