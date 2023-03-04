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

package io.fairyproject.bukkit.library;

import io.fairyproject.library.Library;
import io.fairyproject.mc.library.AdventureLibraryBundle;

import java.util.ArrayList;
import java.util.List;

public class BukkitAdventureLibraryBundle extends AdventureLibraryBundle {

    private final String platformVersion;

    public BukkitAdventureLibraryBundle(String version, String platformVersion) {
        super(version);
        this.platformVersion = platformVersion;
    }

    @Override
    public List<Library> libraries() {
        List<Library> list = new ArrayList<>(super.libraries());
        list.add(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-bungeecord")
                .version(this.platformVersion)
                .build());
        list.add(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-bukkit")
                .version(this.platformVersion)
                .build());
        list.add(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-api")
                .version(this.platformVersion)
                .build());
        list.add(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-facet")
                .version(this.platformVersion)
                .build());
        return list;
    }
}
