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

package org.fairy.bukkit.reflection.wrapper;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

public class ConstructorWrapper<R> extends WrapperAbstract {

	private final Constructor<R> constructor;

	public ConstructorWrapper(Constructor<R> constructor) {
		this.constructor = constructor;
	}

	@Override
	public boolean exists() {
		return this.constructor != null;
	}

	public R newInstance(Object... args) {
		try {
			return this.constructor.newInstance(args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public R newInstanceSilent(Object... args) {
		try {
			return this.constructor.newInstance(args);
		} catch (Exception e) {
		}
		return null;
	}

	public R resolve(Object[]... args) {

		Throwable lastException = null;


		for (Object[] objects : args) {
			try {
				return this.constructor.newInstance(objects);
			} catch (Throwable throwable) {
				lastException = throwable;
			}
		}

		if (lastException != null) {
			throw new RuntimeException(lastException);
		}
		return null;

	}

	@Nullable
	public R resolveSilent(Object[]... args) {

		for (Object[] objects : args) {
			R r = this.newInstanceSilent(objects);
			if (r != null) {
				return r;
			}
		}

		return null;

	}

	public Class<?>[] getParameterTypes() {
		return this.constructor.getParameterTypes();
	}

	public Constructor<R> getConstructor() {
		return constructor;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) { return true; }
		if (object == null || getClass() != object.getClass()) { return false; }

		ConstructorWrapper<?> that = (ConstructorWrapper<?>) object;

		return constructor != null ? constructor.equals(that.constructor) : that.constructor == null;

	}

	@Override
	public int hashCode() {
		return constructor != null ? constructor.hashCode() : 0;
	}
}
