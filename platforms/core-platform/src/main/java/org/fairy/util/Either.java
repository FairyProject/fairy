package org.fairy.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Either<L, R> {

    private static final class Left<L, R> extends Either<L, R> {
        private final L value; private Optional<L> valueOptional; // Paper - reduce the optional allocation...

        public Left(final L value) {
            this.value = value;
        }

        @Override
        public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
            return new Left<>(f1.apply(value));
        }

        @Override
        public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
            return l.apply(value);
        }

        @Override
        public Either<L, R> ifLeft(Consumer<? super L> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public Either<L, R> ifRight(Consumer<? super R> consumer) {
            return this;
        }

        @Override
        public Optional<L> left() {
            return this.valueOptional == null ? this.valueOptional = Optional.of(this.value) : this.valueOptional; // Paper - reduce the optional allocation...
        }

        @Override
        public Optional<R> right() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "Left[" + value + "]";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Left<?, ?> left = (Left<?, ?>) o;
            return Objects.equals(value, left.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private static final class Right<L, R> extends Either<L, R> {
        private final R value; private Optional<R> valueOptional; // Paper - reduce the optional allocation...

        public Right(final R value) {
            this.value = value;
        }

        @Override
        public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
            return new Right<>(f2.apply(value));
        }

        @Override
        public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
            return r.apply(value);
        }

        @Override
        public Either<L, R> ifLeft(Consumer<? super L> consumer) {
            return this;
        }

        @Override
        public Either<L, R> ifRight(Consumer<? super R> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public Optional<L> left() {
            return Optional.empty();
        }

        @Override
        public Optional<R> right() {
            return this.valueOptional == null ? this.valueOptional = Optional.of(this.value) : this.valueOptional; // Paper - reduce the optional allocation...
        }

        @Override
        public String toString() {
            return "Right[" + value + "]";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Right<?, ?> right = (Right<?, ?>) o;
            return Objects.equals(value, right.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private Either() {
    }

    public abstract <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2);

    public abstract <T> T map(final Function<? super L, ? extends T> l, Function<? super R, ? extends T> r);

    public abstract Either<L, R> ifLeft(final Consumer<? super L> consumer);

    public abstract Either<L, R> ifRight(final Consumer<? super R> consumer);

    public abstract Optional<L> left();

    public abstract Optional<R> right();

    public <T> Either<T, R> mapLeft(final Function<? super L, ? extends T> l) {
        return map(t -> left(l.apply(t)), Either::right);
    }

    public <T> Either<L, T> mapRight(final Function<? super R, ? extends T> l) {
        return map(Either::left, t -> right(l.apply(t)));
    }

    public static <L, R> Either<L, R> left(final L value) {
        return new Left<>(value);
    }

    public static <L, R> Either<L, R> right(final R value) {
        return new Right<>(value);
    }

    public L orThrow() {
        return map(l -> l, r -> {
            if (r instanceof Throwable) {
                throw new RuntimeException((Throwable) r);
            }
            throw new RuntimeException(r.toString());
        });
    }

    public Either<R, L> swap() {
        return map(Either::right, Either::left);
    }

    public <L2> Either<L2, R> flatMap(final Function<L, Either<L2, R>> function) {
        return map(function, Either::right);
    }
}
