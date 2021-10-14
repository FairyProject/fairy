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

package io.fairyproject.bukkit.reflection.wrapper;

import io.fairyproject.bukkit.reflection.minecraft.DataWatcher;
import lombok.Getter;

public class DataWatcherWrapper extends WrapperAbstract {

    public static DataWatcherWrapper create(Object entity) {
        try {
            return new DataWatcherWrapper(DataWatcher.newDataWatcher(entity));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Getter
    private final Object dataWatcherObject;

    public DataWatcherWrapper(Object dataWatcherObject) {
        this.dataWatcherObject = dataWatcherObject;
    }

    public void setValue(int index, DataWatcher.V1_9.ValueType type, Object value) {
        try {
            DataWatcher.setValue(this.dataWatcherObject, index, type, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public <T> T getValue(int index, DataWatcher.V1_9.ValueType type, Class<T> classType) {
        try {
            return (T) DataWatcher.getValue(this.dataWatcherObject, index, type);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Object getValue(int index, DataWatcher.V1_9.ValueType type) {
        try {
            return DataWatcher.getValue(this.dataWatcherObject, index, type);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public boolean exists() {
        return dataWatcherObject != null;
    }
}
