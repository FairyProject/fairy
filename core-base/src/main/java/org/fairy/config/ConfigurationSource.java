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

package org.fairy.config;

import java.io.IOException;
import java.util.Map;

/**
 * Implementations of this class save and load {@code Map<String, Object>} maps that
 * represent converted configurations.
 *
 * @param <C> type of the configuration
 */
public interface ConfigurationSource<C extends Configuration<C>> {
    /**
     * Saves the given map.
     *
     * @param config the configuration that the {@code map} object represents
     * @param map    map that is saved
     * @throws IOException if an I/O error occurs when saving the {@code map}
     */
    void saveConfiguration(C config, Map<String, Object> map)
            throws IOException;

    /**
     * Loads the map representing the given {@code Configuration}.
     *
     * @param config the configuration instance that requested the load
     * @return map representing the given {@code Configuration}
     * @throws IOException if an I/O error occurs when loading the map
     */
    Map<String, Object> loadConfiguration(C config)
            throws IOException;
}
