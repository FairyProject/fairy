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

package io.fairyproject.mc.nametag;

import io.fairyproject.mc.MCPlayer;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
final class NameTagUpdate {

    @Nullable
    private final UUID target;

    @Nullable
    private final UUID player;

    private final CompletableFuture<?> future = new CompletableFuture<>();

    public static NameTagUpdate all() {
        return new NameTagUpdate(null, null);
    }

    public static NameTagUpdate create(MCPlayer player, MCPlayer target) {
        return new NameTagUpdate(player, target);
    }

    public static NameTagUpdate createPlayer(MCPlayer player) {
        return new NameTagUpdate(player, null);
    }

    public static NameTagUpdate createTarget(MCPlayer target) {
        return new NameTagUpdate(null, target);
    }

    private NameTagUpdate(MCPlayer player, MCPlayer target) {
        this.player = player == null ? null : player.getUUID();
        this.target = target == null ? null : target.getUUID();
    }
}
