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

package io.fairyproject.util;

import io.fairyproject.scheduler.Scheduler;
import io.fairyproject.scheduler.response.TaskResponse;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;

public class KeyframeValues {

    private final TreeMap<Integer, Keyframe> keyframes = new TreeMap<>(Integer::compareTo);
    private Runnable post;
    private int nextFrame = -1;

    private boolean debug;

    public KeyframeValues add(int time, Keyframe keyframe) {
        time = this.last() + time;
        keyframe.setTime(time);
        this.keyframes.put(time, keyframe);
        return this;
    }

    public KeyframeValues nextFrameTime(int time) {
        this.nextFrame = time;
        return this;
    }

    public KeyframeValues add(Keyframe keyframe) {
        ConditionUtils.is(this.nextFrame != -1, "You must set nextFrameTime before adding a keyframe");
        this.add(this.nextFrame, keyframe);
        return this;
    }

    public KeyframeValues post(Runnable runnable) {
        this.post = runnable;
        return this;
    }

    public KeyframeValues debug() {
        this.debug = true;
        this.keyframes.forEach((key, value) -> System.out.println(key + " " + value.getTime()));
        return this;
    }

    public int last() {
        if (this.keyframes.isEmpty()) {
            return 0;
        }
        return this.keyframes.lastKey();
    }

    public void run(Scheduler scheduler, KeyframeRunner runner) {
        Iterator<Keyframe> iterator = this.keyframes.values().iterator();
        Callable<TaskResponse<Void>> runnable = new Callable<TaskResponse<Void>>() {

            private int time = 0;
            private Keyframe previous = null;
            private Keyframe now = null;

            @Override
            public TaskResponse<Void> call() {
                while (true) {
                    if (now == null) {
                        if (iterator.hasNext()) {
                            now = iterator.next();
                        } else {
                            if (debug) {
                                System.out.println("ended");
                            }

                            if (post != null) {
                                post.run();
                            }

                            return TaskResponse.success(null);
                        }
                    }

                    if (debug) {
                        System.out.println("current " + time + " pending " + now.getTime());
                    }

                    if (this.time == now.getTime()) {
                        runner.run(this.previous, this.now);
                        this.previous = this.now;
                        this.now = null;
                    } else {
                        this.time++;
                        break;
                    }
                }

                return TaskResponse.continueTask();
            }
        };

        scheduler.scheduleAtFixedRate(runnable, Duration.ofMillis(0), Duration.ofMillis(50));
    }

    public static class Keyframe {
        @Setter
        @Getter
        private int time;
        private final Map<String, Object> values = new HashMap<>();

        public <T> T get(String name) {
            if (this.values.containsKey(name)) {
                return (T) this.values.get(name);
            }
            return null;
        }

        public <T> T getOrDefault(String name, T value) {
            T ret = this.get(name);
            return ret == null ? value : ret;
        }

        public Keyframe put(String name, Object value) {
            this.values.put(name, value);
            return this;
        }

        public void remove(String name) {
            this.values.remove(name);
        }

        public boolean has(String name) {
            return this.values.containsKey(name);
        }

    }

    public interface KeyframeRunner {

        void run(@Nullable Keyframe previousKeyframe, Keyframe newKeyframe);

    }

}
