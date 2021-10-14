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

package io.fairyproject.bukkit.command;

import io.fairyproject.bean.Autowired;
import io.fairyproject.bean.Beans;
import io.fairyproject.bukkit.command.event.BukkitCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.util.StringUtil;
import io.fairyproject.command.CommandMeta;
import io.fairyproject.command.CommandService;
import io.fairyproject.command.CommandEvent;
import io.fairyproject.command.parameter.ParameterMeta;

import java.util.*;

final class CommandMap extends SimpleCommandMap {

	protected static final Map<UUID, String[]> parameters = new HashMap<>();

	@Autowired
	private CommandService commandService;

	public CommandMap(Server server) {
		super(server);

		Beans.inject(this);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String cmdLine) {
		if (!(sender instanceof Player)) {
			return (null);
		}

		Player player = (Player) sender;
		parameters.put(player.getUniqueId(), cmdLine.split(" "));

		try {
			int spaceIndex = cmdLine.indexOf(' ');
			Set<String> completions = new HashSet<>();

			boolean doneHere = false;

			CommandLoop:
			for (CommandMeta command : this.commandService.getCommands()) {
				if (command.getPermission() != null && !command.getPermission().isEmpty() && !player.hasPermission(command.getPermission())) {
					continue;
				}

				for (String alias : command.getNames()) {
					String split = alias.split(" ")[0];

					if (spaceIndex != -1) {
						split = alias;
					}

					if (StringUtil.startsWithIgnoreCase(split.trim(), cmdLine.trim()) ||
					    StringUtil.startsWithIgnoreCase(cmdLine.trim(), split.trim())) {
						if (spaceIndex == -1 && cmdLine.length() < alias.length()) {
							// Complete the command
							completions.add("/" + split.toLowerCase());
						} else if (cmdLine.toLowerCase().startsWith(alias.toLowerCase() + " ") &&
						           command.getParameters().size() > 0) {
							// Complete the params
							int paramIndex = (cmdLine.split(" ").length - alias.split(" ").length);

							// If they didn't hit space, complete the param before it.
							if (paramIndex == command.getParameters().size() || !cmdLine.endsWith(" ")) {
								paramIndex = paramIndex - 1;
							}

							if (paramIndex < 0) {
								paramIndex = 0;
							}

							ParameterMeta paramData = command.getParameters().get(paramIndex);
							String[] params = cmdLine.split(" ");

							for (String completion : this.commandService.tabCompleteParameters(player, params,
									cmdLine.endsWith(" ") ? "" : params[params.length - 1],
									paramData.getParameterClass(), paramData.getTabCompleteFlags()
							)) {
								completions.add(completion);
							}

							doneHere = true;

							break CommandLoop;
						} else {
							String halfSplitString =
									split.toLowerCase().replaceFirst(alias.split(" ")[0].toLowerCase(), "").trim();
							String[] splitString = halfSplitString.split(" ");

							String fixedAlias = splitString[splitString.length - 1].trim();
							String lastArg =
									cmdLine.endsWith(" ") ? "" : cmdLine.split(" ")[cmdLine.split(" ").length - 1];

							if (fixedAlias.length() >= lastArg.length()) {
								completions.add(fixedAlias);
							}

							doneHere = true;
						}
					}
				}
			}

			List<String> completionList = new ArrayList<>(completions);

			if (!doneHere) {
				List<String> vanillaCompletionList = super.tabComplete(sender, cmdLine);

				if (vanillaCompletionList == null) {
					vanillaCompletionList = new ArrayList<>();
				}

				for (String vanillaCompletion : vanillaCompletionList) {
					completionList.add(vanillaCompletion);
				}
			}

			Collections.sort(completionList, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return (o2.length() - o1.length());
				}
			});

			completionList.remove("w");

			return (completionList);
		} catch (Exception e) {
			e.printStackTrace();

			return (new ArrayList<>());
		} finally {
			parameters.remove(player.getUniqueId());
		}
	}

	@Override
	public boolean dispatch(CommandSender sender, String commandLine) throws CommandException {
		// (Whiizyyy start) - Supposed to fix error when you execute empty command
		if (commandLine == null || commandLine.equals("") || commandLine.equals(" ")) {
			return false;
		}
		// Whiizyyy end

		final String command = commandLine.substring(1); // Throw error if commandLine is empty

		if (sender instanceof Player) {
			CommandMap.parameters.put(((Player) sender).getUniqueId(), command.split(" "));
		}

		CommandEvent commandEvent = new BukkitCommandEvent(sender, command);
		if (this.commandService.evalCommand(commandEvent)) {
			return true;
		}

		boolean b = super.dispatch(sender, commandLine);
		if (sender instanceof ConsoleCommandSender) {
			ServerCommandEvent event = new ServerCommandEvent(sender, commandLine);
			Bukkit.getPluginManager().callEvent(event);
			return !event.isCancelled();
		}
		return b;
	}

}
