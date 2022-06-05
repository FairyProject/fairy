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

package io.fairyproject.redis.subscription;

import io.fairyproject.redis.RedisService;
import lombok.Getter;
import org.redisson.api.RTopic;

import java.util.function.Consumer;

@Getter
public class RedisPubSub<T> {

    private final String name;
    private final RTopic topic;
    private final Class<T> type;

    public RedisPubSub(String name, RedisService redis, Class<T> type) {
        this.name = name;
        this.topic = redis.getClient().getPatternTopic(name);
        this.type = type;
    }

    public void subscribe(Consumer<T> subscription) {
        this.topic.addListenerAsync(this.type, (channel, message) -> subscription.accept(message));
    }

    public void publish(Object payload) {
        this.topic.publishAsync(payload);
    }

    public void disable() {
        this.topic.removeAllListeners();
    }

}
