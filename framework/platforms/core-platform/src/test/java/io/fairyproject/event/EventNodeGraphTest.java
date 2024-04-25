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

import io.fairyproject.event.Event;
import io.fairyproject.event.EventNode;
import io.fairyproject.event.EventNodeImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeGraphTest {

    @Test
    public void single() {
        EventNode<Event> node = EventNode.all("main");
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0, Collections.emptyList()));
    }

    @Test
    public void singleChild() {
        EventNode<Event> node = EventNode.all("main");
        node.addChild(EventNode.all("child"));
        verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                Collections.singletonList(new EventNodeImpl.Graph("child", "Event", 0, Collections.emptyList())
                )));
    }

    @Test
    public void childrenPriority() {
        {
            EventNode<Event> node = EventNode.all("main");
            node.addChild(EventNode.all("child1").setPriority(5));
            node.addChild(EventNode.all("child2").setPriority(10));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    Arrays.asList(new EventNodeImpl.Graph("child1", "Event", 5, Collections.emptyList()),
                            new EventNodeImpl.Graph("child2", "Event", 10, Collections.emptyList())
                    )));
        }
        {
            EventNode<Event> node = EventNode.all("main");
            node.addChild(EventNode.all("child2").setPriority(10));
            node.addChild(EventNode.all("child1").setPriority(5));
            verifyGraph(node, new EventNodeImpl.Graph("main", "Event", 0,
                    Arrays.asList(new EventNodeImpl.Graph("child1", "Event", 5, new ArrayList<>()),
                            new EventNodeImpl.Graph("child2", "Event", 10, new ArrayList<>())
                    )));
        }
    }

    void verifyGraph(EventNode<?> n, EventNodeImpl.Graph graph) {
        EventNodeImpl<?> node = (EventNodeImpl<?>) n;
        EventNodeImpl.Graph nodeGraph = node.createGraph();
        assertEquals(graph, nodeGraph, "Graphs are not equals");
        assertEquals(EventNodeImpl.createStringGraph(graph), EventNodeImpl.createStringGraph(nodeGraph), "String graphs are not equals");
        assertEquals(n.toString(), EventNodeImpl.createStringGraph(nodeGraph), "The node does not use createStringGraph");
    }
}
