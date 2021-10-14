/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit.command.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BukkitReflection {

	private static final String CRAFT_BUKKIT_PACKAGE;
	private static final String NET_MINECRAFT_SERVER_PACKAGE;

	private static final Class CRAFT_SERVER_CLASS;
	private static final Method CRAFT_SERVER_GET_HANDLE_METHOD;

	private static final Class PLAYER_LIST_CLASS;
	private static final Field PLAYER_LIST_MAX_PLAYERS_FIELD;

	private static final Class CRAFT_PLAYER_CLASS;
	private static final Method CRAFT_PLAYER_GET_HANDLE_METHOD;

	private static final Class ENTITY_PLAYER_CLASS;
	private static final Field ENTITY_PLAYER_PING_FIELD;

	private static final Class CRAFT_ITEM_STACK_CLASS;
	private static final Method CRAFT_ITEM_STACK_AS_NMS_COPY_METHOD;

	static {
		try {
			final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

			CRAFT_BUKKIT_PACKAGE = "org.bukkit.craftbukkit." + version + ".";
			NET_MINECRAFT_SERVER_PACKAGE = "net.minecraft.server." + version + ".";

			CRAFT_SERVER_CLASS = Class.forName(CRAFT_BUKKIT_PACKAGE + "CraftServer");
			CRAFT_SERVER_GET_HANDLE_METHOD = CRAFT_SERVER_CLASS.getDeclaredMethod("getHandle");
			CRAFT_SERVER_GET_HANDLE_METHOD.setAccessible(true);

			PLAYER_LIST_CLASS = Class.forName(NET_MINECRAFT_SERVER_PACKAGE + "PlayerList");
			PLAYER_LIST_MAX_PLAYERS_FIELD = PLAYER_LIST_CLASS.getDeclaredField("maxPlayers");
			PLAYER_LIST_MAX_PLAYERS_FIELD.setAccessible(true);

			CRAFT_PLAYER_CLASS = Class.forName(CRAFT_BUKKIT_PACKAGE + "entity.CraftPlayer");
			CRAFT_PLAYER_GET_HANDLE_METHOD = CRAFT_PLAYER_CLASS.getDeclaredMethod("getHandle");
			CRAFT_PLAYER_GET_HANDLE_METHOD.setAccessible(true);

			ENTITY_PLAYER_CLASS = Class.forName(NET_MINECRAFT_SERVER_PACKAGE + "EntityPlayer");
			ENTITY_PLAYER_PING_FIELD = ENTITY_PLAYER_CLASS.getDeclaredField("ping");
			ENTITY_PLAYER_PING_FIELD.setAccessible(true);

			CRAFT_ITEM_STACK_CLASS = Class.forName(CRAFT_BUKKIT_PACKAGE + "inventory.CraftItemStack");
			CRAFT_ITEM_STACK_AS_NMS_COPY_METHOD = CRAFT_ITEM_STACK_CLASS.getDeclaredMethod("asNMSCopy", ItemStack.class);
			CRAFT_ITEM_STACK_AS_NMS_COPY_METHOD.setAccessible(true);
		} catch (final Exception e) {
			e.printStackTrace();

			throw new RuntimeException("Failed to initialize Bukkit/NMS Reflection");
		}
	}

	public static int getPing(final Player player) {
		try {
			final int ping = ENTITY_PLAYER_PING_FIELD.getInt(CRAFT_PLAYER_GET_HANDLE_METHOD.invoke(player));

			return ping > 0 ? ping : 0;
		} catch (final Exception e) {
			return 1;
		}
	}

	public static void setMaxPlayers(final Server server, final int slots) {
		try {
			PLAYER_LIST_MAX_PLAYERS_FIELD.set(CRAFT_SERVER_GET_HANDLE_METHOD.invoke(server), slots);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static String getItemStackName(final ItemStack itemStack) {
		try {
			return (String) CRAFT_ITEM_STACK_AS_NMS_COPY_METHOD.invoke(itemStack, itemStack);
		} catch (final Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}