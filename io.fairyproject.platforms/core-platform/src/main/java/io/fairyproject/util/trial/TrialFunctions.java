package io.fairyproject.util.trial;

import io.fairyproject.util.entry.Entry;
import io.fairyproject.util.entry.EntryArrayList;
import io.fairyproject.util.exceptionally.ThrowingFunction;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrialFunctions<T, R> {

    public static <T, R> TrialFunctions<T, R> create() {
        return new TrialFunctions<>();
    }

    private final EntryArrayList<ThrowingSupplier<Boolean, ? extends Exception>, ThrowingFunction<T, R, ? extends Exception>> suppliers = new EntryArrayList<>();

    public TrialFunctions<T, R> trial(@NonNull ThrowingSupplier<Boolean, ? extends Exception> booleanSupplier, @NonNull ThrowingFunction<T, R, ? extends Exception> supplier) {
        this.suppliers.add(booleanSupplier, supplier);
        return this;
    }

    public <E extends Exception> TrialFunctions<T, R> trial(@NonNull ThrowingRunnable<E> testRunnable, @NonNull Class<E> failException, @NonNull ThrowingFunction<T, R, ? extends Exception> supplier) {
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

    public ThrowingFunction<T, R, ? extends Exception> find(@NonNull ThrowingFunction<T, R, ? extends Exception> backing) throws Throwable {
        for (Entry<ThrowingSupplier<Boolean, ? extends Exception>, ThrowingFunction<T, R, ? extends Exception>> entry : this.suppliers) {
            if (entry.getKey().get()) {
                return entry.getValue();
            }
        }
        return backing;
    }

}
