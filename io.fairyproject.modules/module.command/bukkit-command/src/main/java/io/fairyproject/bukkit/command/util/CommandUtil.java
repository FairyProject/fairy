package io.fairyproject.bukkit.command.util;

import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.Debug;
import io.fairyproject.util.AccessUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

@UtilityClass
public class CommandUtil {

    private Supplier<CommandMap> COMMAND_MAP_SUPPLIER;

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isShort(String input) {
        try {
            Short.parseShort(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public ItemStack get(final String input) {
        // TODO - remove support for number
        if (CommandUtil.isInteger(input)) {
            return XMaterial.matchXMaterial(Integer.parseInt(input), (byte) 0).orElse(XMaterial.AIR).parseItem();
        }

        if (input.contains(":")) {
            final String[] names = input.split(":");
            if (CommandUtil.isShort(names[1])) {
                if (CommandUtil.isInteger(names[0])) {
                    return XMaterial.matchXMaterial(Integer.parseInt(input), (byte) 0).orElse(XMaterial.AIR).parseItem();
                }
            } else
                return null;
        }

        XMaterial material = XMaterial.matchXMaterial(input).orElse(null);
        if (material == null) {
            return null;
        }

        return material.parseItem();
    }

    public CommandMap getCommandMap() {
        if (Debug.UNIT_TEST)
            return null;

        if (COMMAND_MAP_SUPPLIER == null) {
            try {
                final PluginManager pluginManager = Bukkit.getPluginManager();

                Field field = pluginManager.getClass().getDeclaredField("commandMap");
                AccessUtil.setAccessible(field);

                COMMAND_MAP_SUPPLIER = () -> {
                    try {
                        return (CommandMap) field.get(pluginManager);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                };
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return COMMAND_MAP_SUPPLIER.get();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Map<String, Command> getKnownCommands(CommandMap commandMap) {
        try {
            Field knownMapField = commandMap.getClass().getDeclaredField("knownCommands");
            AccessUtil.setAccessible(knownMapField);

            return (Map<String, Command>) knownMapField.get(commandMap);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public void syncCommands() {
        Server server = Bukkit.getServer();
        try {
            Method syncCommands = server.getClass().getDeclaredMethod("syncCommands");
            AccessUtil.setAccessible(syncCommands);

            syncCommands.invoke(server);
        } catch (Throwable ignored) {
            // maybe it's not a CraftBukkit server or older version
        }
    }

}
