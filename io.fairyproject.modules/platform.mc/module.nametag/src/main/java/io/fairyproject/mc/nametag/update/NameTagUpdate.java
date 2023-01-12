/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.mc.nametag.update;

import io.fairyproject.mc.MCPlayer;

public interface NameTagUpdate {
    static NameTagUpdate all() {
        return new NameTagUpdateImpl(null, null, NameTagUpdateType.ALL);
    }

    static DuoPlayerNameTagUpdate create(MCPlayer player, MCPlayer target) {
        return new NameTagUpdateImpl(player, target, NameTagUpdateType.PLAYER_TO_PLAYER);
    }

    static SinglePlayerNameTagUpdate createPlayerToAll(MCPlayer player) {
        return new NameTagUpdateImpl(player, null, NameTagUpdateType.PLAYER_TO_ALL);
    }

    static SinglePlayerNameTagUpdate createAllToPlayer(MCPlayer target) {
        return new NameTagUpdateImpl(target, null, NameTagUpdateType.ALL_TO_PLAYER);
    }

    java.util.concurrent.CompletableFuture<?> getFuture();

    NameTagUpdateType getType();
}
