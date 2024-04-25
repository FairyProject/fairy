package io.fairyproject.util.filter;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilterUnitImpl<T> implements FilterUnit<T> {

    private final List<Predicate<T>> predicates;
    private final List<Item<T>> items;

    public FilterUnitImpl() {
        this.predicates = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    @Override
    public FilterUnit<T> predicate(@NotNull Predicate<T> predicate) {
        this.predicates.add(predicate);
        return this;
    }

    @Override
    public FilterUnit<T> addAll(@NotNull Iterable<T> iterable) {
        iterable.forEach(this::add);
        return this;
    }

    @Override
    public FilterUnit<T> add(@Nullable T t) {
        Item<T> item = Item.create(t);
        this.items.add(item);
        return this;
    }

    @Override
    public FilterUnit<T> add(@Nullable T t, @NotNull Predicate<T> predicate) {
        Item<T> item = Item.create(t).predicate(predicate);
        this.items.add(item);
        return this;
    }

    @Override
    public FilterUnit<T> add(@NotNull Item<T> item) {
        this.items.add(item);
        return this;
    }

    @Override
    public @NotNull Optional<T> find() {
        return this.findAll().findFirst();
    }

    @Override
    public @NotNull Stream<T> findAll() {
        return this.items.stream()
                .filter(Item::match)
                .map(Item::get)
                .filter(item -> this.predicates.stream().allMatch(predicate -> predicate.test(item)));
    }

    @RequiredArgsConstructor
    public static class ItemImpl<T> implements Item<T> {

        private final T value;
        private List<Predicate<T>> predicates = Collections.emptyList();

        @Override
        public boolean match() {
            return this.predicates.stream().allMatch(predicate -> predicate.test(this.value));
        }

        @Override
        public @Nullable T get() {
            return this.value;
        }

        @Override
        public @NotNull Iterable<Predicate<T>> predicates() {
            return this.predicates;
        }

        @Override
        public Item<T> predicate(@NotNull Predicate<T> predicate) {
            if (this.predicates == Collections.EMPTY_LIST)
                this.predicates = new ArrayList<>();
            this.predicates.add(predicate);
            return this;
        }
    }
}
