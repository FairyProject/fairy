package io.fairytest.container.node;

import io.fairyproject.container.node.Graph;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class GraphTest extends JUnitJupiterBase {

    @Test
    public void graphPrintInOrder() {
        Graph<Integer> graph = new Graph<Integer>() {
            @Override
            public Integer[] depends(Integer parent) {
                switch (parent) {
                    case 5:
                        return new Integer[]{ 2, 0 };
                    case 4:
                        return new Integer[]{ 0, 1 };
                    case 2:
                    case 0:
                        return new Integer[]{ 3 };
                    case 3:
                        return new Integer[]{ 1 };
                    default:
                        return new Integer[0];
                }
            }
        };
        graph.add(4);
        graph.add(5);
        graph.add(3);
        graph.add(2);
        graph.add(1);
        graph.add(0);
        graph.resolve();

        System.out.println(Arrays.toString(graph.getNodes().toArray(new Integer[0])));
        Assertions.assertArrayEquals(new Integer[] { 5, 4, 2, 0, 3, 1 }, graph.getNodes().toArray(new Integer[0]));
    }

    @Test
    public void circularDependencyShouldBePrevented() {
        Graph<Integer> graph = new Graph<Integer>() {
            @Override
            public Integer[] depends(Integer parent) {
                switch (parent) {
                    case 0:
                        return new Integer[]{ 1 };
                    default:
                    case 1:
                        return new Integer[]{ 0 };
                }
            }
        };
        graph.add(0);
        graph.add(1);

        Assertions.assertThrows(IllegalArgumentException.class, graph::resolve);
    }

    @Test
    public void shouldNotAbleToAddAfterResolved() {
        Graph<Integer> graph = new Graph<Integer>() {
            @Override
            public Integer[] depends(Integer parent) {
                return new Integer[0];
            }
        };
        graph.add(0);
        graph.add(1);
        graph.resolve();

        Assertions.assertThrows(IllegalArgumentException.class, () -> graph.add(2));
    }

}
