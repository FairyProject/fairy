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

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;

@UtilityClass
public class FormatUtil {

    private final DecimalFormat SINGLE_DIGIT_DECIMAL_FORMAT = new DecimalFormat("0.0");

    public String formatSingleDigitDecimal(Number number) {
        return SINGLE_DIGIT_DECIMAL_FORMAT.format(number);
    }

    public String formatToSecondsAndMinutes(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public String formatToMinutesAndHours(int seconds) {
        return String.format("%02d:%02d", seconds / 3600, seconds % 3600 / 60);
    }

    public String formatMillisToMinutesAndHours(long millis) {
        int seconds = (int) Math.ceil(millis / 1000.0);
        return String.format("%02d:%02d", seconds / 3600, seconds % 3600 / 60);
    }

    public String formatMillis(long millis) {
        int seconds = (int) Math.ceil(millis / 1000.0);
        return formatSeconds(seconds);
    }

    public String formatMillisToSecondWithDecimal(long millis) {
        return SINGLE_DIGIT_DECIMAL_FORMAT.format(millis / 1000D);
    }

    public String formatSeconds(int seconds) {
        if (seconds >= 3600) {
            return String.format("%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public String getBooleanSymbol(boolean bol) {
        return bol ? "§a√" : "§cX";
    }

    public String formatTimes(long millis) {
        int seconds = (int) Math.ceil(millis / 1000.0);
        return seconds >= 3600 ? (seconds / 3600) + "h" : seconds >= 60 ? (seconds / 60 + 1) + "m" : seconds + "s";
    }

}
