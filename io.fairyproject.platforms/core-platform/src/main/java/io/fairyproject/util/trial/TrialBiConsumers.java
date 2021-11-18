package io.fairyproject.util.trial;

import io.fairyproject.util.entry.Entry;
import io.fairyproject.util.entry.EntryArrayList;
import io.fairyproject.util.exceptionally.ThrowingBiConsumer;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrialBiConsumers<T, R> {

    public static <T, R> TrialBiConsumers<T, R> create() {
        return new TrialBiConsumers<>();
    }

    private final EntryArrayList<ThrowingSupplier<Boolean, ? extends Exception>, ThrowingBiConsumer<T, R, ? extends Exception>> suppliers = new EntryArrayList<>();

    public TrialBiConsumers<T, R> trial(@NonNull ThrowingSupplier<Boolean, ? extends Exception> booleanSupplier, @NonNull ThrowingBiConsumer<T, R, ? extends Exception> supplier) {
        this.suppliers.add(booleanSupplier, supplier);
        return this;
    }

    public <E extends Exception> TrialBiConsumers<T, R> trial(@NonNull ThrowingRunnable<E> testRunnable, @NonNull Class<E> failException, @NonNull ThrowingBiConsumer<T, R, ? extends Exception> supplier) {
        return this.trial(() -> {
            try {
                testRunnable.run();
            } catch (Throwable throwable) {
                if (failException.isInstance(throwable)) {
                    return false;
                }
                throw new IllegalStateException(throwable);
            }
            return true;
        }, supplier);
    }

    public ThrowingBiConsumer<T, R, ? extends Exception> find(@NonNull ThrowingBiConsumer<T, R, ? extends Exception> backing) throws Throwable {
        for (Entry<ThrowingSupplier<Boolean, ? extends Exception>, ThrowingBiConsumer<T, R, ? extends Exception>> entry : this.suppliers) {
            if (entry.getKey().get()) {
                return entry.getValue();
            }
        }
        return backing;
    }

}
