/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package io.fairyproject.state;

import io.fairyproject.log.Log;
import io.fairyproject.state.strategy.StateStrategy;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@Getter
public abstract class StateBase implements State {

    private boolean started = false;
    private boolean ended = false;
    private boolean paused = false;
    private boolean updating = false;

    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();
    private final ReentrantLock lock = new ReentrantLock();

    private final List<StateStrategy> beforeStrategies = new ArrayList<>();
    private final List<StateStrategy> afterStrategies = new ArrayList<>();
    private final ReentrantReadWriteLock strategyLock = new ReentrantReadWriteLock();

    private long startTimestamp;

    @Override
    public void start() {
        this.lock.lock();
        if (this.started || this.ended) {
            return;
        }
        this.started = true;
        this.lock.unlock();

        this.startTimestamp = System.currentTimeMillis();
        try {
            this.forEachStrategies(StateStrategy::onStart, StateStrategy.Type.BEFORE);
            this.onStart();
            this.forEachStrategies(StateStrategy::onStart, StateStrategy.Type.AFTER);
        } catch (Throwable throwable) {
            Log.error("An error occurs while onStart() in State", throwable);
        }
    }

    @Override
    public void update() {
        this.lock.lock();
        if (!this.started || this.ended || this.updating) {
            return;
        }
        this.updating = true;
        this.lock.unlock();

        if (this.isReadyToEnd() && !this.paused) {
            this.end();
            this.updating = false;
            return;
        }

        try {
            this.forEachStrategies(StateStrategy::onUpdate, StateStrategy.Type.BEFORE);
            this.onUpdate();
            this.forEachStrategies(StateStrategy::onUpdate, StateStrategy.Type.AFTER);
        } catch (Throwable throwable) {
            Log.error("An error occurs while onUpdate() in State", throwable);
        }
        this.updating = false;
    }

    @Override
    public void end() {
        this.lock.lock();
        if (!this.started || this.ended) {
            return;
        }
        this.ended = true;
        this.lock.unlock();

        try {
            this.forEachStrategies(StateStrategy::onEnded, StateStrategy.Type.BEFORE);
            this.onEnded();
            this.forEachStrategies(StateStrategy::onEnded, StateStrategy.Type.AFTER);
        } catch (Throwable throwable) {
            Log.error("An error occurs while onEnded() in State", throwable);
        }

        this.compositeTerminable.closeAndReportException();
    }

    @Override
    public long getTimePast() {
        return System.currentTimeMillis() - this.startTimestamp;
    }

    @Override
    public final boolean isReadyToEnd() {
        return this.ended || this.canEnd();
    }

    @Override
    public void pause() {
        this.paused = true;
        try {
            this.forEachStrategies(StateStrategy::onPause, StateStrategy.Type.BEFORE);
            this.onPause();
            this.forEachStrategies(StateStrategy::onPause, StateStrategy.Type.AFTER);
        } catch (Throwable throwable) {
            Log.error("An error occurs while onPause() in State", throwable);
        }
    }

    @Override
    public void unpause() {
        this.paused = false;
        try {
            this.forEachStrategies(StateStrategy::onUnpause, StateStrategy.Type.BEFORE);
            this.onUnpause();
            this.forEachStrategies(StateStrategy::onUnpause, StateStrategy.Type.AFTER);
        } catch (Throwable throwable) {
            Log.error("An error occurs while onUnpause() in State", throwable);
        }
    }

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Override
    public void close() throws Exception {
        this.end();
    }

    @Override
    public boolean isClosed() {
        return this.isEnded();
    }

    public void forEachStrategies(@NotNull Consumer<StateStrategy> consumer, @NotNull StateStrategy.Type type) {
        this.strategyLock.readLock().lock();
        try {
            this.strategiesInternal(type).forEach(consumer);
        } finally {
            this.strategyLock.readLock().unlock();
        }
    }

    @Override
    public void addStrategy(@NotNull StateStrategy strategy, @NotNull StateStrategy.Type type) {
        this.strategyLock.writeLock().lock();
        try {
            this.strategiesInternal(type).add(strategy);
            this.strategiesInternal(type).sort((a, b) -> b.priority() - a.priority());
        } finally {
            this.strategyLock.writeLock().unlock();
        }
    }

    @Override
    public void removeStrategy(@NotNull StateStrategy strategy, @NotNull StateStrategy.Type type) {
        this.strategyLock.writeLock().lock();
        try {
            this.strategiesInternal(type).remove(strategy);
        } finally {
            this.strategyLock.writeLock().unlock();
        }
    }

    @Override
    public @NotNull Collection<StateStrategy> strategies(@NotNull StateStrategy.Type type) {
        return Collections.unmodifiableList(this.strategiesInternal(type));
    }

    public @NotNull List<StateStrategy> strategiesInternal(@NotNull StateStrategy.Type type) {
        switch (type) {
            case BEFORE:
                return this.beforeStrategies;
            case AFTER:
                return this.afterStrategies;
            default:
                throw new IllegalArgumentException();
        }
    }

    protected abstract void onStart();

    protected abstract void onUpdate();

    public void onSuspend() {

    }

    protected void onPause() {

    }

    protected void onUnpause() {

    }

    protected abstract void onEnded();

    protected boolean canEnd() {
        return false;
    }

}
