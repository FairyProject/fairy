package io.fairytest.util.filter;

import io.fairyproject.util.filter.FilterUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterUnitTest {

    @Test
    public void findReturnsMatchingValueInPredicatesAndShouldBeOrdered() {
        String result = FilterUnit.<String>create()
                .predicate(s -> s.contains("A"))
                .add("N")
                .add("A") // <- matching here
                .add("B")
                .add("AA") // <- this also matches but should not return
                .add("C")
                .add("AAA") // <- this too
                .find()
                .orElse(null);

        // test
        Assertions.assertEquals("A", result);
    }

    @Test
    public void addAllAndShouldContainsInFindAll() {
        int countMatches = 10;
        int countNotMatches = 90;
        // generate texts with A (which meet the condition)
        List<String> matching = new ArrayList<>(countMatches);
        for (int i = 0; i < countMatches; i++)
            matching.add("A" + i + "A");
        // generate texts without A
        List<String> notMatching = new ArrayList<>(countNotMatches);
        for (int i = 0; i < countNotMatches; i++)
            notMatching.add("X" + i + "X");
        // do test
        List<String> retVal = FilterUnit.<String>create()
                .addAll(notMatching)
                .addAll(matching)
                .predicate(s -> s.contains("A"))
                .findAll()
                .collect(Collectors.toList());

        // test
        Assertions.assertEquals(matching, retVal);
    }

    @Test
    public void individualPredicateForItem() {
        List<String> list = FilterUnit.<String>create()
                .predicate(a -> a.contains("A")) // default condition.
                .add("A matching value") // <- this value matches, should contain.
                .add("B")
                .add("Another matching value..?", item -> false) // if without the predicate, this value should match. but due to the predicate instantly returns false, it should NOT contain.
                .add("C")
                .add(FilterUnit.Item.create("Another matching value...!").predicate(item -> item.contains("Another"))) // and this one with predicate and the predicate should be matching, so this one will also contain.
                .findAll()
                .collect(Collectors.toList());

        // test
        Assertions.assertLinesMatch(Arrays.asList(
                "A matching value",
//                "Another matching value..?", // NOPE! this one isn't here!
                "Another matching value...!"
        ), list);
    }

}
