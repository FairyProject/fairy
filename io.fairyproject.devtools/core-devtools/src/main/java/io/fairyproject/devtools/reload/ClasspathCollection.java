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

package io.fairyproject.devtools.reload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ClasspathCollection {

    private final List<URL> urls = new ArrayList<>();

    public ClasspathCollection(String path) {
        if (path == null)
            return;

        String[] paths = path.split(":");
        for (String p : paths) {
            try {
                urls.add(Paths.get(p).toUri().toURL());
            } catch (Throwable throwable) {
                throw new IllegalStateException(throwable);
            }
        }
    }

    public void addURL(URL url) {
        urls.add(url);
    }

    public URL[] getURLs() {
        return urls.toArray(new URL[0]);
    }

}
