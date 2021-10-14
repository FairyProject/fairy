package io.fairyproject.bootstrap.bukkit.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@UtilityClass
public class BootstrapUtil {

    public File getPluginDirectory() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> minecraftServer;
        try {
            // 1.17+
            minecraftServer = Class.forName("net.minecraft.server.MinecraftServer");
        } catch (ClassNotFoundException ex) {
            String v = Bukkit.getServer().getClass().getPackage().getName();
            v = v.substring(v.lastIndexOf('.') + 1);
            try {
                minecraftServer = Class.forName("net.minecraft.server." + v + ".MinecraftServer");
            } catch (ClassNotFoundException ex2) {
                throw new IllegalStateException("Couldn't find MinecraftServer class!");
            }
        }

        Class<?> optionSet;
        try {
            optionSet = Class.forName("org.bukkit.craftbukkit.libs.joptsimple.OptionSet");
        } catch (ClassNotFoundException ex) {
            try {
                optionSet = Class.forName("joptsimple.OptionSet");
            } catch (ClassNotFoundException ex2) {
                throw new IllegalStateException("Couldn't find OptionSet class!");
            }
        }

        Method valueOf = optionSet.getMethod("valueOf", String.class);

        Field optionSetField = null;
        Field serverField = null;
        for (Field field : minecraftServer.getFields()) {
            if (field.getType() == optionSet) {
                optionSetField = field;
            } else if (field.getType() == minecraftServer && Modifier.isStatic(field.getModifiers())) {
                serverField = field;
            }
        }

        if (optionSetField == null) {
            throw new IllegalStateException("Couldn't found OptionSet field!");
        }

        if (serverField == null) {
            throw new IllegalStateException("Couldn't found MinecraftServer field!");
        }

        optionSetField.setAccessible(true);
        serverField.setAccessible(true);

        final Object server = serverField.get(null);
        final Object optionSetObj = optionSetField.get(server);

        return (File) valueOf.invoke(optionSetObj, "plugins");
    }

}
