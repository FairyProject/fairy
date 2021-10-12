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

package org.fairy.bukkit.timer;

public abstract class TimerBase extends Timer {

    private final TimerList timerList;

    public TimerBase(long startTime, long duration, TimerList timerList) {
        super(startTime, duration);
        this.timerList = timerList;

        if (this.timerList != null) {
            this.timerList.add(this);
        }
    }

    public TimerBase(long startTime, long duration) {
        this(startTime, duration, null);
    }

    public TimerBase(long duration, TimerList timerList) {
        this(System.currentTimeMillis(), duration, timerList);
    }

    public TimerBase(long duration) {
        this(System.currentTimeMillis(), duration);
    }

    @Override
    protected final void onPreClear() {
        if (this.timerList != null) {
            this.timerList.remove(this);
        }
    }

    @Override
    protected final void onPostStart() {
        TIMER_SERVICE.add(this);
    }
}
