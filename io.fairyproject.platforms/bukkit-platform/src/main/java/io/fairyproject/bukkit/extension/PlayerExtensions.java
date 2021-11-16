package io.fairyproject.bukkit.extension;

import io.fairyproject.mc.MCPlayer;
import org.bukkit.entity.Player;

public class PlayerExtensions {

    public static MCPlayer asMCPlayer(Player originalPlayer) {
        return MCPlayer.from(originalPlayer);
    }

    public static String goodMorningAntoine(Player originalPlayer) {
        return "yo";
    }

}
