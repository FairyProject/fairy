package io.fairyproject.util.thread.executor;

import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListeningDecorator extends AbstractListeningExecutorService {

    public static ListeningExecutorService create(ExecutorService executorService) {
        return new ListeningDecorator(executorService);
    }

    private final ExecutorService delegate;

    private ListeningDecorator(ExecutorService delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public final boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public final boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public final boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public final void shutdown() {
        delegate.shutdown();
    }

    @Override
    public final List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public final void execute(Runnable command) {
        delegate.execute(command);
    }
}