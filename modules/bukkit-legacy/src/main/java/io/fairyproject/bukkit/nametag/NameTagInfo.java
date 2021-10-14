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

package io.fairyproject.bukkit.nametag;

import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public final class NameTagInfo {

    private static final AtomicInteger TEAM_INDEX = new AtomicInteger(0);

    private String name;
    private String prefix;
    private String suffix;

    private Set<String> nameSet;

    protected NameTagInfo(final String prefix, final String suffix) {
        this.name = "ImanityTeam-" + TEAM_INDEX.getAndIncrement();
        this.prefix = prefix;
        this.suffix = suffix;
        this.nameSet = new HashSet<>();
    }

    public void addName(String name) {
        this.nameSet.add(name);
    }

    public void removeName(String name) {
        this.nameSet.remove(name);
    }

    public boolean hasName(String name) {
        return this.nameSet.contains(name);
    }
}
