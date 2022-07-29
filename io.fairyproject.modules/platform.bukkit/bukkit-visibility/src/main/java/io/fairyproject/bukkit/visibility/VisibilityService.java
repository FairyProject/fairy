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

package io.fairyproject.bukkit.visibility;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.collection.ContainerObjCollector;
import org.bukkit.entity.Player;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.container.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class VisibilityService {

    private List<VisibilityAdapter> visibilityAdapters;

    @PreInitialize
    public void preInit() {
        this.visibilityAdapters = new LinkedList<>();

        ContainerContext.get().objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(VisibilityAdapter.class))
                .withAddHandler(ContainerObjCollector.warpInstance(VisibilityAdapter.class, this::register))
                .withRemoveHandler(ContainerObjCollector.warpInstance(VisibilityAdapter.class, this::unregister))
        );
    }

    public void register(VisibilityAdapter visibilityAdapter) {
        this.visibilityAdapters.add(visibilityAdapter);
    }

    public void unregister(VisibilityAdapter visibilityAdapter) {
        this.visibilityAdapters.remove(visibilityAdapter);
    }

    public boolean isUsed() {
        return !this.visibilityAdapters.isEmpty();
    }

    public boolean treatAsOnline(final Player target, final Player viewer) {
        return viewer.canSee(target);
    }

    public void updateAll() {
        if (!this.isUsed()) {
            return;
        }

        for (Player player : Imanity.getPlayers()) {
            this.updateFromThirdSide(player);
        }
    }

    public void update(Player player) {
        if (this.isUsed()) {
            this.updateFromFirstSide(player);
            this.updateFromThirdSide(player);
        }
    }

    public void updateFromFirstSide(Player player) {
        for (Player target : Imanity.getPlayers()) {

            if (target == player) {
                continue;
            }

            if (this.canSee(player, target)) {
                player.showPlayer(target);
            } else {
                player.hidePlayer(target);
            }

        }
    }

    public void updateFromThirdSide(Player player) {
        for (Player target : Imanity.getPlayers()) {
            if (target == player) {
                continue;
            }
            if (this.canSee(target, player)) {
                target.showPlayer(player);
            } else {
                target.hidePlayer(player);
            }
        }
    }


    public boolean canSee(Player receiver, Player target) {
        for (VisibilityAdapter visibilityAdapter : this.visibilityAdapters) {
            VisibilityOption option = visibilityAdapter.check(receiver, target);

            switch (option) {
                case SHOW:
                    return true;
                case HIDE:
                    return false;
                case NOTHING:
                    break;
            }
        }

        return true;
    }
}
