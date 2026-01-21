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

package io.fairyproject.hytale.logger;

import com.hypixel.hytale.logger.HytaleLogger;
import io.fairyproject.log.ILogger;

import java.util.logging.Level;

public class HytaleILogger implements ILogger {

    private final HytaleLogger logger;

    public HytaleILogger() {
        this.logger = HytaleLogger.get("Fairy");
    }

    @Override
    public void info(String message, Object... replace) {
        logger.at(Level.INFO).log(String.format(message, replace));
    }

    @Override
    public void debug(String message, Object... replace) {
        logger.at(Level.FINE).log(String.format(message, replace));
    }

    @Override
    public void warn(String message, Object... replace) {
        logger.at(Level.WARNING).log(String.format(message, replace));
    }

    @Override
    public void error(String message, Object... replace) {
        logger.at(Level.SEVERE).log(String.format(message, replace));
    }

    @Override
    public void info(String message, Throwable throwable, Object... replace) {
        logger.at(Level.INFO).withCause(throwable).log(String.format(message, replace));
    }

    @Override
    public void debug(String message, Throwable throwable, Object... replace) {
        logger.at(Level.FINE).withCause(throwable).log(String.format(message, replace));
    }

    @Override
    public void warn(String message, Throwable throwable, Object... replace) {
        logger.at(Level.WARNING).withCause(throwable).log(String.format(message, replace));
    }

    @Override
    public void error(String message, Throwable throwable, Object... replace) {
        logger.at(Level.SEVERE).withCause(throwable).log(String.format(message, replace));
    }

    @Override
    public void info(Throwable throwable) {
        logger.at(Level.INFO).withCause(throwable).log("");
    }

    @Override
    public void debug(Throwable throwable) {
        logger.at(Level.FINE).withCause(throwable).log("");
    }

    @Override
    public void warn(Throwable throwable) {
        logger.at(Level.WARNING).withCause(throwable).log("");
    }

    @Override
    public void error(Throwable throwable) {
        logger.at(Level.SEVERE).withCause(throwable).log("");
    }
}
