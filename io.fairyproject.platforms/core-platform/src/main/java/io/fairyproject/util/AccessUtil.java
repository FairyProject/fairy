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

import io.fairyproject.reflect.wrapper.ReflectWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Helper class to set fields, methods &amp; constructors accessible
 */
public abstract class AccessUtil {

	/**
	 * Sets the field accessible and removes final modifiers
	 *
	 * @param field Field to set accessible
	 * @return the Field
	 * @throws ReflectiveOperationException (usually never)
	 */
	public static Field setAccessible(Field field) throws ReflectiveOperationException {
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		int modifiers = field.getModifiers();
		if (!Modifier.isFinal(modifiers)) {
			return field;
		}
		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
		} catch (Throwable e) {
			if (e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
				// Java 16 compatibility
				final Method method = ReflectWrapper.get().findMethod(Field.class, "setAccessible0", boolean.class);
				ReflectWrapper.get().invokeMethod(field, method, true);
			}
			if ("modifiers".equals(e.getMessage()) || (e.getCause() != null && e.getCause().getMessage() != null &&  e.getCause().getMessage().equals("modifiers"))) {
				// https://github.com/ViaVersion/ViaVersion/blob/e07c994ddc50e00b53b728d08ab044e66c35c30f/bungee/src/main/java/us/myles/ViaVersion/bungee/platform/BungeeViaInjector.java
				// Java 12 compatibility *this is fine*
				Field[] fields;
				final Method getDeclaredFields0 = ReflectWrapper.get().findMethod(Class.class, "getDeclaredFields0", boolean.class);
				final Object o = ReflectWrapper.get().invokeMethod(Field.class, getDeclaredFields0, false);
				fields = (Field[]) o;
				for (Field classField : fields) {
					if ("modifiers".equals(classField.getName())) {
						ReflectWrapper.get().setField(field, classField, modifiers & ~Modifier.FINAL);
						break;
					}
				}
			} else {
				throw e;
			}
		}
		return field;
	}

	/**
	 * Sets the method accessible
	 *
	 * @param method Method to set accessible
	 * @return the Method
	 * @throws ReflectiveOperationException (usually never)
	 */
	public static Method setAccessible(Method method) throws ReflectiveOperationException {
		if (method.isAccessible()) {
			return method;
		}

		method.setAccessible(true);
		return method;
	}

	/**
	 * Sets the constructor accessible
	 *
	 * @param constructor Constructor to set accessible
	 * @return the Constructor
	 * @throws ReflectiveOperationException (usually never)
	 */
	public static Constructor setAccessible(Constructor constructor) throws ReflectiveOperationException {
		if (constructor.isAccessible()) {
			return constructor;
		}

		constructor.setAccessible(true);
		return constructor;
	}

}
