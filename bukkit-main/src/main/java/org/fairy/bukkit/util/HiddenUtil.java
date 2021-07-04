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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HiddenUtil {

    public static void hidePlayerFromAnySide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.hidePlayer(player1);
                player1.hidePlayer(player);
            }
        }
    }

    public static void showPlayerFromAnySide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.showPlayer(player1);
                player1.showPlayer(player);
            }
        }
    }

    public static void hidePlayerFromThirdSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player1.hidePlayer(player);
            }
        }
    }

    public static void showPlayerFromThirdSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player1.showPlayer(player);
            }
        }
    }

    public static void hidePlayerFromFirstSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.hidePlayer(player1);
            }
        }
    }

    public static void showPlayerFromFirstSide(Player player) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1 != player) {
                player.showPlayer(player1);
            }
        }
    }

}
