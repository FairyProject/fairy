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

package io.fairyproject.bukkit.timer;

import com.google.common.collect.ImmutableSet;
import io.fairyproject.bukkit.timer.event.*;
import io.fairyproject.container.Autowired;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class Timer implements Terminable, TerminableConsumer {

    @Autowired
    protected static TimerService TIMER_SERVICE;

    protected static final Set<Integer> COUNTDOWNS;

    static {
        COUNTDOWNS = ImmutableSet.of(3200,
                1600,
                1200,
                600,
                300,
                180,
                120,
                60,
                30,
                15,
                10,
                9,
                8,
                7,
                6,
                5,
                4,
                3,
                2,
                1,
                0);
    }

    private final long startTime;
    private long pauseTime;
    private long duration;
    private long elapsedTime;
    private int currentAnnounceSeconds;
    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

    private boolean closed;

    public Timer(long startTime, long duration) {
        this.startTime = startTime;
        this.duration = duration;
        this.pauseTime = -1;
        this.elapsedTime = this.startTime + this.duration;
    }

    public Timer(long duration) {
        this(System.currentTimeMillis(), duration);
    }

    public final boolean shouldAnnounce(int seconds) {
        if (this.currentAnnounceSeconds == seconds) {
            return false;
        }

        this.currentAnnounceSeconds = seconds;
        return COUNTDOWNS.contains(seconds) && this.isAnnouncing(seconds);
    }

    protected boolean isAnnouncing(int time) {
        return false;
    }

    public boolean isElapsed() {
        return System.currentTimeMillis() > elapsedTime;
    }

    public long getTimeMillisRemaining() {
        return this.elapsedTime - System.currentTimeMillis();
    }

    public int getSecondsRemaining() {
        return (int) TimeUnit.MILLISECONDS.toSeconds(this.getTimeMillisRemaining());
    }

    public void restart(long startTime) {
        long diff = startTime - this.startTime;
        this.extend(diff);
    }

    public void extend(long millis) {
        this.extend(millis, TimerExtendEvent.Reason.PLUGIN);
    }

    public void extend(long millis, TimerExtendEvent.Reason reason) {
        TimerExtendEvent event = new TimerExtendEvent(this, this.duration, this.duration + millis, millis, reason);
        event.call();

        if (event.isCancelled()) {
            return;
        }

        millis = event.getExtended();
        this.duration += millis;
        this.elapsedTime = this.startTime + this.duration;
    }

    public void setDuration(long duration) {
        this.setDuration(duration, TimerExtendEvent.Reason.PLUGIN);
    }

    public void setDuration(long duration, TimerExtendEvent.Reason reason) {
        TimerExtendEvent event = new TimerExtendEvent(this, this.duration, duration, duration - this.duration, reason);
        event.call();

        if (event.isCancelled()) {
            return;
        }

        duration = this.duration + event.getExtended();
        this.duration = duration;
        this.elapsedTime = this.startTime + this.duration;
    }

    /**
     * get the announcement message for timer to announce
     *
     * @param player the Player that receives this message
     * @param seconds the Time seconds to announce
     * @return the message component
     */
    public Component getAnnounceMessage(Player player, int seconds) {
        return Component.text("Time Remaining: ", NamedTextColor.YELLOW)
                .append(Component.text(seconds));
    }

    public void sendMessage(Player player, Component message, int seconds) {
        MCPlayer.from(player).sendMessage(message);
    }

    public final void tick() {
        int seconds = this.getSecondsRemaining();
        if (this.shouldAnnounce(seconds)) {
            Collection<? extends Player> players = this.getReceivers();
            if (players != null) {
                for (Player player : players) {
                    Component component = this.getAnnounceMessage(player, seconds);
                    this.sendMessage(player, component, seconds);
                }
            }
        }

        this.onTick();
    }

    protected void onTick() {

    }

    public abstract Collection<? extends Player> getReceivers();

    @Override
    public void close() {
        this.clear();
    }

    public boolean isClosed() {
        return this.closed;
    }

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    public final boolean clear() {
        return this.clear(true, TimerClearEvent.Reason.PLUGIN);
    }

    protected final boolean clear(boolean removeFromHandler, TimerClearEvent.Reason reason) {
        return new TimerClearEvent(this, reason).supplyCancelled(cancel -> {
            if (cancel)
                return false;
            if (!this.shouldClear())
                return false;
            if (removeFromHandler) {
                TIMER_SERVICE.clear(this);
            }
            this.onPreClear();
            this.closed = true;
            this.onClear();
            this.compositeTerminable.closeAndReportException();
            return true;
        });
    }

    protected abstract void onPreClear();

    protected boolean shouldClear() {
        return true;
    }

    protected void onClear() {

    }

    public final boolean unpause() {
        if (this.pauseTime == -1) {
            return false;
        }
        return new TimerUnpauseEvent(this).supplyCancelled(cancel -> {
            if (cancel)
                return false;
            if (!this.shouldUnpause())
                return false;
            final long toExtend = System.currentTimeMillis() - this.pauseTime;
            this.extend(toExtend, TimerExtendEvent.Reason.UNPAUSE);

            this.pauseTime = -1;
            this.onUnpause();
            return true;
        });
    }

    protected boolean shouldUnpause() {
        return true;
    }

    protected void onUnpause() {

    }

    public final boolean pause() {
        if (this.pauseTime != -1) {
            return false;
        }
        return new TimerPauseEvent(this).supplyCancelled(cancel -> {
            if (cancel)
                return false;
            if (!this.shouldPause())
                return false;
            this.pauseTime = System.currentTimeMillis();
            this.onPause();
            return true;
        });
    }

    public final boolean isPaused() {
        return this.pauseTime != -1;
    }

    protected boolean shouldPause() {
        return true;
    }

    protected void onPause() {

    }

    public final boolean start() {
        return new TimerStartEvent(this).supplyCancelled(cancel -> {
            if (cancel)
                return false;
            if (!this.shouldStart())
                return false;
            this.onStart();
            this.onPostStart();
            return true;
        });
    }

    protected abstract void onPostStart();

    protected boolean shouldStart() {
        return true;
    }

    protected void onStart() {

    }

    public final boolean elapsed() {
        return new TimerElapsedEvent(this).supplyCancelled(cancel -> {
            if (cancel) {
                return false;
            }
            if (!this.shouldElapsed()) {
                return false;
            }
            this.onElapsed();
            return true;
        });
    }

    protected boolean shouldElapsed() {
        return true;
    }

    protected void onElapsed() {

    }
}

