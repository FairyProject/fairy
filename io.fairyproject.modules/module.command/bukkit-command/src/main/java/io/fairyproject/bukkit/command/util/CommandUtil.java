package io.fairyproject.bukkit.command.util;

import com.cryptomorin.xseries.XMaterial;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@UtilityClass
public class CommandUtil {

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

    private Supplier<CommandMap> COMMAND_MAP_SUPPLIER;

    public CommandMap getCommandMap() {
        if (COMMAND_MAP_SUPPLIER == null) {
//            try {
//                Bukkit.comm();
//                COMMAND_MAP_SUPPLIER = Bukkit::getCommandMap;
//            } catch (NoSuchMethodError ex) {
                try {
                    Method method = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
                    method.setAccessible(true);

                    COMMAND_MAP_SUPPLIER = () -> {
                        try {
                            return (CommandMap) method.invoke(Bukkit.getServer());
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                    };
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
//            }
        }

        return COMMAND_MAP_SUPPLIER.get();
    }

}
