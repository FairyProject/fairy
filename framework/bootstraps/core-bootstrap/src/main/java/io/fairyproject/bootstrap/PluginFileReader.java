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

package io.fairyproject.bootstrap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Read plugin description file.
 *
 * @since 0.7.0
 * @author LeeGod
 */
public class PluginFileReader {

    private static final String FAIRY_JSON_PATH = "fairy.json";

    /**
     * Read plugin description file.
     *
     * @param baseClass the base class to read resource from.
     * @return the plugin description, null if not found.
     */
    public JsonObject read(Class<?> baseClass) {
        // Read the descriptor co-located in the same jar as the plugin class first, so that in
        // shared-classloader dev runs a sibling plugin's fairy.json on the parent classpath cannot
        // shadow this plugin's own descriptor (which caused "Plugin X already registered!" crashes).
        URL classUrl = baseClass.getResource(baseClass.getSimpleName() + ".class");
        if (classUrl != null && classUrl.toString().startsWith("jar:")) {
            String jarUrl = classUrl.toString();
            try {
                String prefix = jarUrl.substring(0, jarUrl.indexOf("!/") + 2);
                URLConnection connection = new URL(prefix + FAIRY_JSON_PATH).openConnection();
                connection.setUseCaches(false);

                return new Gson().fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
            } catch (Throwable ignored) {
                // fall through to the classloader lookup
            }
        }

        URL url = baseClass.getClassLoader().getResource(FAIRY_JSON_PATH);

        if (url == null) {
            return null;
        }

        try {
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);

            return new Gson().fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load " + FAIRY_JSON_PATH, throwable);
        }
    }

}
