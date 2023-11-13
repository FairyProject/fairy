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

package io.fairyproject.event;

import io.fairyproject.event.EventNode;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeQueryTest {

    @Test
    public void find() {
        var node = EventNode.all("main");
        assertEquals(new ArrayList<>(), node.findChildren("test"));

        var child1 = EventNode.all("test");
        var child2 = EventNode.all("test");
        var child3 = EventNode.all("test3");

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        assertEquals(Arrays.asList(child1, child2), node.findChildren("test"));
        assertEquals(Collections.singletonList(child3), node.findChildren("test3"));

        node.removeChild(child1);
        assertEquals(Collections.singletonList(child2), node.findChildren("test"));
        assertEquals(Collections.singletonList(child3), node.findChildren("test3"));
    }

    @Test
    public void replace() {
        var node = EventNode.all("main");

        var child1 = EventNode.all("test");
        var child2 = EventNode.all("test");
        var child3 = EventNode.all("test3");

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        var tmp1 = EventNode.all("tmp1");
        var tmp2 = EventNode.all("tmp2");

        node.replaceChildren("test", tmp1);
        assertEquals(Collections.singletonList(child2), node.findChildren("test"));
        assertEquals(Collections.singletonList(tmp1), node.findChildren("tmp1"));

        node.replaceChildren("test3", tmp2);
        assertEquals(Collections.singletonList(child2), node.findChildren("test"));
        assertEquals(Collections.singletonList(tmp1), node.findChildren("tmp1"));
        assertEquals(Collections.emptyList(), node.findChildren("test3"));
        assertEquals(Collections.singletonList(tmp2), node.findChildren("tmp2"));
    }
}
