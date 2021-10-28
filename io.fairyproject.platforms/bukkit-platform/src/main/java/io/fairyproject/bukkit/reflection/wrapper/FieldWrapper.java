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

import java.lang.reflect.Field;

public class FieldWrapper<R> extends WrapperAbstract {

	private final Field field;

	public FieldWrapper(Field field) {
		this.field = field;
	}

	@Override
	public boolean exists() {
		return this.field != null;
	}

	public String getName() {
		return this.field.getName();
	}

	public R get(Object object) {
		try {
			return (R) this.field.get(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public R getSilent(Object object) {
		try {
			return (R) this.field.get(object);
		} catch (Exception e) {
		}
		return null;
	}

	public void set(Object object, R value) {
		try {
			this.field.set(object, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setSilent(Object object, R value) {
		try {
			this.field.set(object, value);
		} catch (Exception e) {
		}
	}

	public Field getField() {
		return field;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) { return true; }
		if (object == null || getClass() != object.getClass()) { return false; }

		FieldWrapper<?> that = (FieldWrapper<?>) object;

		if (field != null ? !field.equals(that.field) : that.field != null) { return false; }

		return true;
	}

	@Override
	public int hashCode() {
		return field != null ? field.hashCode() : 0;
	}
}
