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

package io.fairyproject.bukkit.command.parameters;

import io.fairyproject.bean.Component;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GameModeArgTransformer extends BukkitArgTransformer<GameMode> {

	private static final Map<String, GameMode> MAP = new HashMap<>();

	static {
		MAP.put("creative", GameMode.CREATIVE);
		MAP.put("1", GameMode.CREATIVE);

		MAP.put("survival", GameMode.SURVIVAL);
		MAP.put("0", GameMode.SURVIVAL);

		MAP.put("adventure", GameMode.ADVENTURE);
		MAP.put("2", GameMode.ADVENTURE);

		if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().isOrAbove(MCVersion.V1_8)) {
			MAP.put("spectator", GameMode.SPECTATOR);
			MAP.put("3", GameMode.SPECTATOR);
		}
	}

	public GameMode transform(CommandSender sender, String source) {
		if (!MAP.containsKey(source.toLowerCase())) {
			return this.fail(source + " is not a valid game mode.");
		}

		return MAP.get(source.toLowerCase());
	}

	public List<String> tabComplete(Player sender, String source) {
		return (MAP.keySet().stream().filter(string -> StringUtils.startsWithIgnoreCase(string, source))
		           .collect(Collectors.toList()));
	}

	@Override
	public Class[] type() {
		return new Class[] {GameMode.class};
	}
}