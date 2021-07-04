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

package org.fairy.bukkit.timer.impl;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.fairy.bukkit.timer.Timer;
import org.fairy.bukkit.timer.TimerList;
import org.fairy.bukkit.timer.event.TimerElapsedEvent;
import org.fairy.bukkit.timer.event.TimerExtendEvent;
import org.fairy.bukkit.Imanity;
import org.fairy.util.CountdownData;
import org.fairy.util.RV;
import org.fairy.util.StringUtil;

import java.util.Collection;

@Getter
public abstract class AbstractTimer implements Timer {

    private boolean paused;
    private long beginTime;
    private long duration;
    private long elapsedTime;

    private boolean shouldAnnounce;

    private TimerList timerList;
    private CountdownData countdownData;

    public AbstractTimer(long beginTime, long duration, TimerList timerList) {
        this.beginTime = beginTime;
        this.duration = duration;
        this.elapsedTime = this.beginTime + this.duration;
        this.timerList = timerList;

        if (this.timerList != null) {
            this.timerList.add(this);
        }
    }

    public AbstractTimer(long beginTime, long duration) {
        this(beginTime, duration, null);
    }

    public AbstractTimer(long duration, TimerList timerList) {
        this(System.currentTimeMillis(), duration, timerList);
    }

    public AbstractTimer(long duration) {
        this(System.currentTimeMillis(), duration);
    }

    public final void announcing(boolean shouldAnnounce) {
        if (this.shouldAnnounce = shouldAnnounce
            && countdownData == null) {
            countdownData = new CountdownData(this.secondsRemaining() + 1);
        }
    }

    public boolean isTimerElapsed() {
        return System.currentTimeMillis() > elapsedTime;
    }

    @Override
    public void pause() {
        this.paused = true;
    }

    @Override
    public long timeRemaining() {
        return this.elapsedTime - System.currentTimeMillis();
    }

    @Override
    public int secondsRemaining() {
        return (int) Math.ceil(this.timeRemaining() / 1000D);
    }

    @Override
    public void extend(long millis) {
        TimerExtendEvent event = new TimerExtendEvent(this, this.duration, this.duration + millis, millis);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        millis = event.getExtended();
        this.duration += millis;
        this.elapsedTime = this.beginTime + this.duration;
    }

    @Override
    public void duration(long duration) {
        TimerExtendEvent event = new TimerExtendEvent(this, this.duration, duration, duration - this.duration);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        duration = this.duration + event.getExtended();
        this.duration = duration;
        this.elapsedTime = this.beginTime + this.duration;
    }

    public String announceMessage(Player player, int seconds) {
        return "Time Remaining: <seconds>";
    }

    public void sendMessage(Player player, String message, int seconds) {
        player.sendMessage(message);
    }

    public String getScoreboardText(Player player) {
        return "&fTimer: &e" + this.secondsRemaining() + "s";
    }

    @Override
    public void clear(boolean removeFromHandler) {
        if (removeFromHandler) {
            Imanity.TIMER_HANDLER.clear(this);
        }
        if (this.timerList != null) {
            this.timerList.remove(this);
        }
    }

    @Override
    public void tick() {

        int seconds = this.secondsRemaining();
        if (countdownData != null &&
                !this.countdownData.isEnded()
            && this.countdownData.canAnnounce(seconds)) {

            Collection<? extends Player> players = this.getReceivers();
            if (players != null) {
                players.forEach(player -> this.sendMessage(player, StringUtil.replace(this.announceMessage(player, seconds),
                        RV.o("<player>", player.getName()),
                        RV.o("<seconds>", seconds)
                ), seconds));
            }

        }

    }

    @Override
    public boolean finish() {
        TimerElapsedEvent event = new TimerElapsedEvent(this);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        this.elapsed();
        return true;
    }

    public void elapsed() {

    }
}
