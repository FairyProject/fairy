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

package org.fairy.bukkit.player.storage;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface ThreadedPlayerStorageConfiguration<T> {

    String getName();

    T loadAsync(UUID uuid, String name);

    void saveAsync(UUID uuid, T t);

    default String getLoginRejectMessage(UUID uuid, String name, ThreadedPlayerStorage.LoginRejectReason rejectReason) {
        switch (rejectReason) {
            case ERROR:
                return "An error has been occur in server side, Please try again later.";
            case DATA_UNLOADED:
                return "You data got unloaded during processing, Please try again later.";
        }

        return "Your login request has been rejected.";
    }

    boolean shouldUnloadOnQuit(Player player);

}
