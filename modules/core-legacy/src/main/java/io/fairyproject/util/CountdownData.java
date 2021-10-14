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

package io.fairyproject.util;

public class CountdownData {

    private static final int[] COUNTDOWNS = {
            3200,
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
            0
    };

    private int currentCount;

    public CountdownData(int startCount) {
        for (int i = 0; i < COUNTDOWNS.length; i++) {
            if (COUNTDOWNS[i] <= startCount) {
                this.currentCount = i + 1;
                return;
            }
        }
        throw new IllegalStateException("The count " + startCount + " does not match to any of the COUNTDOWN we listed!");
    }

    public boolean isEnded() {
        return currentCount >= COUNTDOWNS.length - 1;
    }

    public boolean canAnnounce(int count) { // currentCount = 5, count = 9
        this.validCount(count);
        if (count <= COUNTDOWNS[this.currentCount]) {
            this.currentCount++;
            return true;
        }
        return false;
    }

    public void validCount(int count) {
        if (count > COUNTDOWNS[this.currentCount - 1]) {
            for (int i = this.currentCount; i > 0; i--) {
                if (count <= COUNTDOWNS[i]) {
                    this.currentCount = i;
                    break;
                }
            }
        }
    }

}
