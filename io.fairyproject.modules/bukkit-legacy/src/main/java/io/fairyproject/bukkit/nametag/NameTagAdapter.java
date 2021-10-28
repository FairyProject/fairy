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

import lombok.Getter;
import org.bukkit.entity.Player;
import io.fairyproject.bean.Autowired;

import java.beans.ConstructorProperties;

@Getter
public abstract class NameTagAdapter {

    @Autowired
    private static NameTagService nameTagService;

    public static final NameTagInfo createNametag(final String prefix, final String suffix) {
        return nameTagService.getOrCreate(prefix, suffix);
    }

    private final String name;
    private final int weight;

    public abstract NameTagInfo fetch(final Player receiver, final Player target);

    @ConstructorProperties({ "name", "weight" })
    public NameTagAdapter(final String name, final int weight) {
        this.name = name;
        this.weight = weight;
//        ImanityCommon.BEAN_CONTEXT.injectBeans(this); // maybe not
    }

}
