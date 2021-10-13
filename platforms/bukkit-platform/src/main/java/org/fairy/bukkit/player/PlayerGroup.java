package org.fairy.bukkit.player;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.fairy.bukkit.util.Chat;
import org.fairy.mc.PlaceholderEntry;
import org.fairy.bukkit.util.sound.SoundData;
import org.fairy.locale.Locales;

import java.util.List;

public interface PlayerGroup extends Iterable<Player> {

    List<Player> getPlayers();

    default void broadcast(String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, Sound sound, PlaceholderEntry... rvs) {
        broadcast(messageLocaleKey, titleLocaleKey, subTitleLocaleKey, SoundData.of(sound), rvs);
    }

    default void broadcast(String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, SoundData soundData, PlaceholderEntry... rvs) {
        broadcast(messageLocaleKey, titleLocaleKey, subTitleLocaleKey, 20, 200, 20, true, soundData, rvs);
    }

    default void broadcast(String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        if (messageLocaleKey != null) {
            Chat.broadcast(players, messageLocaleKey, rvs);
        }
        Chat.broadcastTitle(players, titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(players);
        }
    }

    default void broadcastRaw(String message, String title, String subTitle, Sound sound, PlaceholderEntry... rvs) {
        broadcastRaw(message, title, subTitle, SoundData.of(sound), rvs);
    }

    default void broadcastRaw(String message, String title, String subTitle, SoundData soundData, PlaceholderEntry... rvs) {
        broadcastRaw(message, title, subTitle, 20, 200, 20, true, soundData, rvs);
    }

    default void broadcastRaw(String message, String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        if (message != null) {
            Chat.broadcastRaw(players, message, rvs);
        }
        Chat.broadcastTitleRaw(players, title, subTitle, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(players);
        }
    }

    default void broadcastTitle(String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        for (Player player : players) {
            Chat.sendTitleRaw(player, Locales.translate(player, titleLocaleKey), Locales.translate(player, subTitleLocaleKey), fadeIn, stay, fadeOut, clean, rvs);
        }
    }

    default void broadcastTitle(String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, PlaceholderEntry... rvs) {
        broadcastTitle(titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, true, rvs);
    }

    default void broadcastTitle(String titleLocaleKey, String subTitleLocaleKey, PlaceholderEntry... rvs) {
        broadcastTitle(titleLocaleKey, subTitleLocaleKey, 20, 200, 20, rvs);
    }

    default void broadcastTitle(String titleLocaleKey, PlaceholderEntry... rvs) {
        broadcastTitle(titleLocaleKey, null, rvs);
    }

    default void broadcastSubTitle(String subTitleLocaleKey, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        Chat.broadcastTitle(players, null, subTitleLocaleKey, rvs);
    }

    default void broadcast(String localeKey, SoundData soundData, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        Chat.broadcast(players, localeKey, rvs);
        soundData.play(players);
    }

    default void broadcast(String localeKey, Sound sound, PlaceholderEntry... rvs) {
        broadcast(localeKey, sound, 1f, 1f, rvs);
    }

    default void broadcast(String localeKey, Sound sound, float volume, float pitch, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        Chat.broadcast(players, localeKey, rvs);
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    default void broadcast(String localeKey, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        for (Player player : players) {
            Chat.sendRaw(player, Locales.translate(player, localeKey), rvs);
        }
    }

    default void broadcastRaw(String rawMessage, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        for (Player player : players) {
            Chat.sendRaw(player, rawMessage, rvs);
        }
    }

    default void broadcastRaw(String rawMessage, SoundData soundData, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        Chat.broadcastRaw(players, rawMessage, rvs);
        soundData.play(players);
    }

    default void broadcastRaw(String rawMessage, Sound sound, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        Chat.broadcastRaw(players, rawMessage, sound, 1f, 1f, rvs);
    }

    default void broadcastRaw(String rawMessage, Sound sound, float volume, float pitch, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        Chat.broadcastRaw(players, rawMessage, rvs);
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    default void broadcastTitleRaw(String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        for (Player player : players) {
            Chat.sendTitleRaw(player, title, subTitle, fadeIn, stay, fadeOut, clean, rvs);
        }
    }

    default void broadcastTitleRaw(String title, String subTitle, int fadeIn, int stay, int fadeOut, PlaceholderEntry... rvs) {
        broadcastTitleRaw(title, subTitle, fadeIn, stay, fadeOut, true, rvs);
    }

    default void broadcastTitleRaw(String title, String subTitle, PlaceholderEntry... rvs) {
        broadcastTitleRaw(title, subTitle, 20, 200, 20, rvs);
    }

    default void broadcastTitleRaw(String title, PlaceholderEntry... rvs) {
        broadcastTitleRaw(title, null, rvs);
    }

    default void broadcastSubTitleRaw(String subTitle, PlaceholderEntry... rvs) {
        List<? extends Player> players = this.getPlayers();
        Chat.broadcastTitleRaw(players, null, subTitle, rvs);
    }

}
