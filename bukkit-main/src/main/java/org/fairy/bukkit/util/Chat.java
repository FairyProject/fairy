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

package org.fairy.bukkit.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.fairy.locale.Locales;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.packet.PacketService;
import org.fairy.bukkit.packet.wrapper.server.WrappedPacketOutTitle;
import org.fairy.bukkit.reflection.wrapper.ChatComponentWrapper;
import org.fairy.bukkit.util.sound.SoundData;
import org.fairy.util.CC;
import org.fairy.util.RV;
import org.fairy.util.StringUtil;

import java.util.List;

@UtilityClass
public class Chat {

    public void actionbar(String localeKey, LocaleRV... rvs) {
        actionbar(Imanity.getPlayers(), localeKey, rvs);
    }

    public void actionbarRaw(String message, LocaleRV... rvs) {
        actionbarRaw(Imanity.getPlayers(), message, rvs);
    }

    public void actionbar(Iterable<? extends Player> players, String localeKey, LocaleRV... rvs) {
        for (Player player : players) {
            actionbarRaw(player, localeKey, rvs);
        }
    }

    public void actionbarRaw(Iterable<? extends Player> players, String message, LocaleRV... rvs) {
        for (Player player : players) {
            actionbarRaw(player, message, rvs);
        }
    }

    public void actionbar(Player player, String localeKey, LocaleRV... rvs) {
        actionbarRaw(player, Locales.translate(player, localeKey), rvs);
    }

    public void actionbarRaw(Player player, String message, LocaleRV... rvs) {
        Imanity.IMPLEMENTATION.sendActionBar(player, setupMessageRaw(player, message, rvs));
    }

    public void broadcast(String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, Sound sound, LocaleRV... rvs) {
        broadcast(messageLocaleKey, titleLocaleKey, subTitleLocaleKey, SoundData.of(sound), rvs);
    }

    public void broadcast(String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, SoundData soundData, LocaleRV... rvs) {
        broadcast(messageLocaleKey, titleLocaleKey, subTitleLocaleKey, 20, 200, 20, true, soundData, rvs);
    }

    public void broadcast(String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        if (messageLocaleKey != null) {
            broadcast(players, messageLocaleKey, rvs);
        }
        broadcastTitle(players, titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(players);
        }
    }

    public void broadcastRaw(String message, String title, String subTitle, Sound sound, LocaleRV... rvs) {
        broadcastRaw(message, title, subTitle, SoundData.of(sound), rvs);
    }

    public void broadcastRaw(String message, String title, String subTitle, SoundData soundData, LocaleRV... rvs) {
        broadcastRaw(message, title, subTitle, 20, 200, 20, true, soundData, rvs);
    }

    public void broadcastRaw(String message, String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        if (message != null) {
            broadcastRaw(players, message, rvs);
        }
        broadcastTitleRaw(players, title, subTitle, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(players);
        }
    }

    public void broadcastTitle(String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        for (Player player : players) {
            sendTitleRaw(player, Locales.translate(player, titleLocaleKey), Locales.translate(player, subTitleLocaleKey), fadeIn, stay, fadeOut, clean, rvs);
        }
    }

    public void broadcastTitle(String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, LocaleRV... rvs) {
        broadcastTitle(titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, true, rvs);
    }

    public void broadcastTitle(String titleLocaleKey, String subTitleLocaleKey, LocaleRV... rvs) {
        broadcastTitle(titleLocaleKey, subTitleLocaleKey, 20, 200, 20, rvs);
    }

    public void broadcastTitle(String titleLocaleKey, LocaleRV... rvs) {
        broadcastTitle(titleLocaleKey, null, rvs);
    }

    public void broadcastSubTitle(String subTitleLocaleKey, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        broadcastTitle(players, null, subTitleLocaleKey, rvs);
    }

    public void broadcast(String localeKey, SoundData soundData, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        broadcast(players, localeKey, rvs);
        soundData.play(players);
    }

    public void broadcast(String localeKey, Sound sound, LocaleRV... rvs) {
        broadcast(localeKey, sound, 1f, 1f, rvs);
    }

    public void broadcast(String localeKey, Sound sound, float volume, float pitch, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        broadcast(players, localeKey, rvs);
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void broadcast(String localeKey, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        for (Player player : players) {
            sendRaw(player, Locales.translate(player, localeKey), rvs);
        }
    }

    public void broadcastRaw(String rawMessage, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        for (Player player : players) {
            sendRaw(player, rawMessage, rvs);
        }
    }

    public void broadcastRaw(String rawMessage, SoundData soundData, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        broadcastRaw(players, rawMessage, rvs);
        soundData.play(players);
    }

    public void broadcastRaw(String rawMessage, Sound sound, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        broadcastRaw(players, rawMessage, sound, 1f, 1f, rvs);
    }

    public void broadcastRaw(String rawMessage, Sound sound, float volume, float pitch, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        broadcastRaw(players, rawMessage, rvs);
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void broadcastTitleRaw(String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        for (Player player : players) {
            sendTitleRaw(player, title, subTitle, fadeIn, stay, fadeOut, clean, rvs);
        }
    }

    public void broadcastTitleRaw(String title, String subTitle, int fadeIn, int stay, int fadeOut, LocaleRV... rvs) {
        broadcastTitleRaw(title, subTitle, fadeIn, stay, fadeOut, true, rvs);
    }

    public void broadcastTitleRaw(String title, String subTitle, LocaleRV... rvs) {
        broadcastTitleRaw(title, subTitle, 20, 200, 20, rvs);
    }

    public void broadcastTitleRaw(String title, LocaleRV... rvs) {
        broadcastTitleRaw(title, null, rvs);
    }

    public void broadcastSubTitleRaw(String subTitle, LocaleRV... rvs) {
        List<? extends Player> players = Imanity.getPlayers();
        broadcastTitleRaw(players, null, subTitle, rvs);
    }

    // ---------------------------------------------------------------------------------------------------

    public void broadcast(Iterable<? extends Player> players, String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, Sound sound, LocaleRV... rvs) {
        broadcast(players, messageLocaleKey, titleLocaleKey, subTitleLocaleKey, SoundData.of(sound), rvs);
    }

    public void broadcast(Iterable<? extends Player> players, String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, SoundData soundData, LocaleRV... rvs) {
        broadcast(players, messageLocaleKey, titleLocaleKey, subTitleLocaleKey, 20, 200, 20, true, soundData, rvs);
    }

    public void broadcast(Iterable<? extends Player> players, String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, LocaleRV... rvs) {
        broadcast(players, messageLocaleKey, rvs);
        broadcastTitle(players, titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(players);
        }
    }

    public void broadcastRaw(Iterable<? extends Player> players, String message, String title, String subTitle, Sound sound, LocaleRV... rvs) {
        broadcastRaw(players, message, title, subTitle, SoundData.of(sound), rvs);
    }

    public void broadcastRaw(Iterable<? extends Player> players, String message, String title, String subTitle, SoundData soundData, LocaleRV... rvs) {
        broadcastRaw(players, message, title, subTitle, 20, 200, 20, true, soundData, rvs);
    }

    public void broadcastRaw(Iterable<? extends Player> players, String message, String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, LocaleRV... rvs) {
        broadcastRaw(players, message, rvs);
        broadcastTitleRaw(players, title, subTitle, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(players);
        }
    }

    public void broadcastTitle(Iterable<? extends Player> players, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, LocaleRV... rvs) {
        for (Player player : players) {
            sendTitleRaw(player, Locales.translate(player, titleLocaleKey), Locales.translate(player, subTitleLocaleKey), fadeIn, stay, fadeOut, clean, rvs);
        }
    }

    public void broadcastTitle(Iterable<? extends Player> players, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, LocaleRV... rvs) {
        broadcastTitle(players, titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, true, rvs);
    }

    public void broadcastTitle(Iterable<? extends Player> players, String titleLocaleKey, String subTitleLocaleKey, LocaleRV... rvs) {
        broadcastTitle(players, titleLocaleKey, subTitleLocaleKey, 20, 200, 20, rvs);
    }

    public void broadcastTitle(Iterable<? extends Player> players, String titleLocaleKey, LocaleRV... rvs) {
        broadcastTitle(players, titleLocaleKey, null, rvs);
    }

    public void broadcastSubTitle(Iterable<? extends Player> players, String subTitleLocaleKey, LocaleRV... rvs) {
        broadcastTitle(players, null, subTitleLocaleKey, rvs);
    }

    public void broadcast(Iterable<? extends Player> players, String localeKey, SoundData soundData, LocaleRV... rvs) {
        broadcast(players, localeKey, rvs);
        soundData.play(players);
    }

    public void broadcast(Iterable<? extends Player> players, String localeKey, Sound sound, LocaleRV... rvs) {
        broadcast(players, localeKey, sound, 1f, 1f, rvs);
    }

    public void broadcast(Iterable<? extends Player> players, String localeKey, Sound sound, float volume, float pitch, LocaleRV... rvs) {
        broadcast(players, localeKey, rvs);
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void broadcast(Iterable<? extends Player> players, String localeKey, LocaleRV... rvs) {
        if (localeKey == null) {
            return;
        }
        for (Player player : players) {
            sendRaw(player, Locales.translate(player, localeKey), rvs);
        }
    }

    public void broadcastRaw(Iterable<? extends Player> players, String rawMessage, LocaleRV... rvs) {
        for (Player player : players) {
            sendRaw(player, rawMessage, rvs);
        }
    }

    public void broadcastRaw(Iterable<? extends Player> players, String rawMessage, SoundData soundData, LocaleRV... rvs) {
        broadcastRaw(players, rawMessage, rvs);
        soundData.play(players);
    }

    public void broadcastRaw(Iterable<? extends Player> players, String rawMessage, Sound sound, LocaleRV... rvs) {
        broadcastRaw(players, rawMessage, sound, 1f, 1f, rvs);
    }

    public void broadcastRaw(Iterable<? extends Player> players, String rawMessage, Sound sound, float volume, float pitch, LocaleRV... rvs) {
        broadcastRaw(players, rawMessage, rvs);
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void broadcastTitleRaw(Iterable<? extends Player> players, String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, LocaleRV... rvs) {
        for (Player player : players) {
            sendTitleRaw(player, title, subTitle, fadeIn, stay, fadeOut, clean, rvs);
        }
    }

    public void broadcastTitleRaw(Iterable<? extends Player> players, String title, String subTitle, int fadeIn, int stay, int fadeOut, LocaleRV... rvs) {
        broadcastTitleRaw(players, title, subTitle, fadeIn, stay, fadeOut, true, rvs);
    }

    public void broadcastTitleRaw(Iterable<? extends Player> players, String title, String subTitle, LocaleRV... rvs) {
        broadcastTitleRaw(players, title, subTitle, 20, 200, 20, rvs);
    }

    public void broadcastTitleRaw(Iterable<? extends Player> players, String title, LocaleRV... rvs) {
        broadcastTitleRaw(players, title, null, rvs);
    }

    public void broadcastSubTitleRaw(Iterable<? extends Player> players, String subTitle, LocaleRV... rvs) {
        broadcastTitleRaw(players, null, subTitle, rvs);
    }

    // ---------------------------------------------------------------------------------------------------

    /*

    Compat all

     */

    public void send(Player player, String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, Sound sound, LocaleRV... rvs) {
        send(player, messageLocaleKey, titleLocaleKey, subTitleLocaleKey, SoundData.of(sound), rvs);
    }

    public void send(Player player, String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, SoundData soundData, LocaleRV... rvs) {
        send(player, messageLocaleKey, titleLocaleKey, subTitleLocaleKey, 20, 200, 20, true, soundData, rvs);
    }

    public void send(Player player, String messageLocaleKey, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, LocaleRV... rvs) {
        send(player, messageLocaleKey, rvs);
        sendTitle(player, titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(player);
        }
    }

    public void sendRaw(Player player, String message, String title, String subTitle, Sound sound, LocaleRV... rvs) {
        sendRaw(player, message, title, subTitle, SoundData.of(sound), rvs);
    }

    public void sendRaw(Player player, String message, String title, String subTitle, SoundData soundData, LocaleRV... rvs) {
        sendRaw(player, message, title, subTitle, 20, 200, 20, true, soundData, rvs);
    }

    public void sendRaw(Player player, String message, String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, SoundData soundData, LocaleRV... rvs) {
        sendRaw(player, message, rvs);
        sendTitleRaw(player, title, subTitle, fadeIn, stay, fadeOut, clean, rvs);
        if (soundData != null) {
            soundData.play(player);
        }
    }

    /*

    Separate Title and Chat Message

    Locale

     */

    public void sendTitle(Player player, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, boolean clean, LocaleRV... rvs) {
        sendTitleRaw(player, Locales.translate(player, titleLocaleKey), Locales.translate(player, subTitleLocaleKey), fadeIn, stay, fadeOut, clean, rvs);
    }

    public void sendTitle(Player player, String titleLocaleKey, String subTitleLocaleKey, int fadeIn, int stay, int fadeOut, LocaleRV... rvs) {
        sendTitle(player, titleLocaleKey, subTitleLocaleKey, fadeIn, stay, fadeOut, true, rvs);
    }

    public void sendTitle(Player player, String titleLocaleKey, String subTitleLocaleKey, LocaleRV... rvs) {
        sendTitle(player, titleLocaleKey, subTitleLocaleKey, 20, 200, 20, rvs);
    }

    public void sendTitle(Player player, String titleLocaleKey, LocaleRV... rvs) {
        sendTitle(player, titleLocaleKey, null, rvs);
    }

    public void sendSubTitle(Player player, String subTitleLocaleKey, LocaleRV... rvs) {
        sendTitle(player, null, subTitleLocaleKey, rvs);
    }

    public void send(Player player, String localeKey, SoundData soundData, LocaleRV... rvs) {
        send(player, localeKey, rvs);
        soundData.play(player);
    }

    public void send(Player player, String localeKey, Sound sound, LocaleRV... rvs) {
        send(player, localeKey, sound, 1f, 1f, rvs);
    }

    public void send(Player player, String localeKey, Sound sound, float volume, float pitch, LocaleRV... rvs) {
        send(player, localeKey, rvs);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void send(Player player, String localeKey, LocaleRV... rvs) {
        if (localeKey == null) {
            return;
        }
        sendRaw(player, Locales.translate(player, localeKey), rvs);
    }

    /*

    Separate Title and Chat Message

    Raw

     */

    public void sendRaw(Player player, String rawMessage, SoundData soundData, LocaleRV... rvs) {
        sendRaw(player, rawMessage, rvs);
        soundData.play(player);
    }

    public void sendRaw(Player player, String rawMessage, Sound sound, LocaleRV... rvs) {
        sendRaw(player, rawMessage, sound, 1f, 1f, rvs);
    }

    public void sendRaw(Player player, String rawMessage, Sound sound, float volume, float pitch, LocaleRV... rvs) {
        sendRaw(player, rawMessage, rvs);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void sendRaw(Player player, String rawMessage, LocaleRV... rvs) {
        player.sendMessage(setupMessageRaw(player, rawMessage, rvs));
    }

    public void sendTitleRaw(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut, boolean clean, LocaleRV... rvs) {
        title = setupMessageRaw(player, title, rvs);
        subTitle = setupMessageRaw(player, subTitle, rvs);

        PacketService.send(player, WrappedPacketOutTitle.builder()
                .action(WrappedPacketOutTitle.Action.TIMES)
                .message(null)
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut)
                .build());

        if (title != null) {
            PacketService.send(player, WrappedPacketOutTitle.builder()
                    .action(WrappedPacketOutTitle.Action.TITLE)
                    .message(ChatComponentWrapper.fromText(title))
                    .build());
        }

        if (subTitle != null) {
            PacketService.send(player, WrappedPacketOutTitle.builder()
                    .action(WrappedPacketOutTitle.Action.SUBTITLE)
                    .message(ChatComponentWrapper.fromText(subTitle))
                    .build());
        } else if (clean) {
            PacketService.send(player, WrappedPacketOutTitle.builder()
                    .action(WrappedPacketOutTitle.Action.SUBTITLE)
                    .message(ChatComponentWrapper.fromText(""))
                    .build());
        }
    }

    public void sendTitleRaw(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut, LocaleRV... rvs) {
        sendTitleRaw(player, title, subTitle, fadeIn, stay, fadeOut, true, rvs);
    }

    public void sendTitleRaw(Player player, String title, String subTitle, LocaleRV... rvs) {
        sendTitleRaw(player, title, subTitle, 20, 200, 20, rvs);
    }

    public void sendTitleRaw(Player player, String title, LocaleRV... rvs) {
        sendTitleRaw(player, title, null, rvs);
    }

    public void sendSubTitleRaw(Player player, String subTitle, LocaleRV... rvs) {
        sendTitleRaw(player, null, subTitle, rvs);
    }

    public String setupMessageRaw(Player player, String message, @NonNull LocaleRV... rvs) {
        if (message == null) {
            return null;
        }

        for (LocaleRV rv : rvs) {
            message = StringUtil.replace(message, rv.getTarget(), rv.getReplacement(player));
        }

        return CC.translate(message);
    }

    public static String translate(Player player, String key) {
        return CC.translate(Locales.translate(player.getUniqueId(), key));
    }

    public static Iterable<String> translateLines(Player player, String key) {
        return StringUtil.separateLines(Chat.translate(player, key), "\n");
    }

    public static String translate(Player player, String key, RV... replaceValues) {
        return StringUtil.replace(Chat.translate(player, key), replaceValues);
    }

    public static Iterable<String> translateLines(Player player, String key, RV... replaceValues) {
        return StringUtil.separateLines(Chat.translate(player, key, replaceValues), "\n");
    }

    public static String translate(Player player, String key, LocaleRV... replaceValues) {

        String result = Chat.translate(player, key);

        for (LocaleRV rv : replaceValues) {
            result = StringUtil.replace(result, rv.getTarget(), rv.getReplacement(player));
        }

        return result;
    }

    public static Iterable<String> translateLines(Player player, String key, LocaleRV... replaceValues) {
        return StringUtil.separateLines(Chat.translate(player, key, replaceValues), "\n");
    }


}
