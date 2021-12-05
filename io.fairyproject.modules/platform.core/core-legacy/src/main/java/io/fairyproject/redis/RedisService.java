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

package io.fairyproject.redis;

import io.fairyproject.Fairy;
import io.fairyproject.container.*;
import io.fairyproject.jackson.JacksonService;
import io.fairyproject.library.Library;
import lombok.Getter;
import lombok.SneakyThrows;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.io.File;
import java.util.Arrays;

@Service(name = "redis")
@ServiceDependency(JacksonService.class)
@Getter
public class RedisService {

    private RedissonClient client;

    private final File configFile;

    public RedisService() {
        this.configFile = new File(Fairy.getPlatform().getDataFolder(), "redisson.yml");
        if (!configFile.exists()) {
            Fairy.getPlatform().saveResource("redisson.yml", false);
        }
    }

    @ShouldInitialize
    public boolean shouldInitialize() {
        return true;
//        return Fairy.getBaseConfiguration().isUseRedis();
    }

    @SneakyThrows
    @PreInitialize
    public void preInit() {
        this.client = Redisson.create(Config.fromYAML(configFile).setCodec(new JsonJacksonCodec(JacksonService.INSTANCE.getMainMapper())));
    }

    @SneakyThrows
    @PostInitialize
    public void init() {
    }

    @PostDestroy
    public void stop() {
        this.client.shutdown();
    }

    public RReadWriteLock getLock(String name) {
        return this.client.getReadWriteLock(name);
    }

    public RMap<String, Object> getMap(String name) {
        return this.client.getMap(name);
    }

    public Iterable<String> getKeys(String pattern) {
        return this.client.getKeys().getKeysByPattern(pattern);
    }

}
