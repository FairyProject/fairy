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

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;

public class TimerList extends LinkedList<Timer> {

    public boolean isTimerRunning(Class<? extends Timer> timerClass) {
        for (Timer timer : this) {
            if (timerClass.isInstance(timer)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public <E extends Timer> E getTimer(Class<E> timerClass) {
        for (Timer timer : this) {
            if (timerClass.isInstance(timer)) {
                return timerClass.cast(timer);
            }
        }

        return null;
    }

    public boolean removeTimer(Class<? extends Timer> timerClass) {
        Iterator<Timer> iterator = this.iterator();
        while (iterator.hasNext()) {
            Timer timer = iterator.next();
            if (timerClass.isInstance(timer)) {
                if (!timer.isElapsed()) {
                    if (!timer.clear()) {
                        continue;
                    }
                }
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        Iterator<Timer> iterator = this.iterator();

        while (iterator.hasNext()) {
            iterator.next().clear();
            iterator.remove();
        }
    }
}
