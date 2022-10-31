package io.fairytest.event;

import com.google.common.collect.Lists;
import io.fairyproject.event.EventNode;
import lombok.var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventNodeQueryTest {

    @Test
    public void find() {
        var node = EventNode.all("main");
        assertEquals(Lists.newArrayList(), node.findChildren("test"));

        var child1 = EventNode.all("test");
        var child2 = EventNode.all("test");
        var child3 = EventNode.all("test3");

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);

        assertEquals(Lists.newArrayList(child1, child2), node.findChildren("test"));
        assertEquals(Lists.newArrayList(child3), node.findChildren("test3"));

        node.removeChild(child1);
        assertEquals(Lists.newArrayList(child2), node.findChildren("test"));
        assertEquals(Lists.newArrayList(child3), node.findChildren("test3"));
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
        assertEquals(Lists.newArrayList(child2), node.findChildren("test"));
        assertEquals(Lists.newArrayList(tmp1), node.findChildren("tmp1"));

        node.replaceChildren("test3", tmp2);
        assertEquals(Lists.newArrayList(child2), node.findChildren("test"));
        assertEquals(Lists.newArrayList(tmp1), node.findChildren("tmp1"));
        assertEquals(Lists.newArrayList(), node.findChildren("test3"));
        assertEquals(Lists.newArrayList(tmp2), node.findChildren("tmp2"));
    }
}
