package io.fairytest.event;

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
