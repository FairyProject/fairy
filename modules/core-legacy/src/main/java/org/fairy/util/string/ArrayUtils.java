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

package org.fairy.util.string;

import org.fairy.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ArrayUtils {

	public static String arrayJoin(String[] args, int start) {
		return IntStream.range(start, args.length).mapToObj(i -> args[i] + " ").collect(Collectors.joining()).trim();
	}

	@SafeVarargs
	public static <T> List<T> asList(T... args) {
		final List<T> list = new ArrayList<>();
		Collections.addAll(list, args);
		return list;
	}

	public static <T> T[] arrayAppend(T[] array, T obj) {
		final T[] arrayNew = arrayExpand(array, 1);
		arrayNew[array.length] = obj;
		return arrayNew;
	}

	public static <T> T[] arrayAddFirst(T[] array, T obj) {
		final T[] arrayNew = arrayExpandAtFirst(array, 1);
		arrayNew[0] = obj;
		return arrayNew;
	}

	@SuppressWarnings("unchecked" )
	public static <T> T arrayExpand(T oldArray, int expand) {
		final int length = Array.getLength(oldArray);
		final Object newArray = Array.newInstance(oldArray.getClass().getComponentType(), length + expand);
		System.arraycopy(oldArray, 0, newArray, 0, length);
		return (T) newArray;
	}

	@SuppressWarnings("unchecked")
	public static <T> T arrayExpandAtFirst(T oldArray, int expand) {
		final int length = Array.getLength(oldArray);
		final Object newArray = Array.newInstance(oldArray.getClass().getComponentType(), length + expand);
		System.arraycopy(oldArray, 0, newArray, expand, length);
		return (T) newArray;
	}

	@SuppressWarnings("unchecked")
	public static <T> T cloneAsByte(T obj) throws Exception {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(obj);
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		return (T) objectInputStream.readObject();
	}

	public static <T> T skipEmpty(T obj) {
		return skipEmpty(obj, null);
	}

	public static <T> T[] skipEmpty(T[] obj) {
		return skipEmpty(obj, null);
	}

	public static <T> T skipEmpty(T obj, T def) {
		return StringUtil.isEmpty(String.valueOf(obj)) ? def : obj;
	}

	public static <T> T[] skipEmpty(T[] obj, T[] def) {
		if (obj.length == 0) {
			return def;
		}
		final T firstElement = skipEmpty(obj[0]);
		return firstElement == null ? def : obj;
	}

	// *********************************
	//
	//           Deprecated
	//
	// *********************************

	@Deprecated
	public static String[] addFirst(String[] args, String... value) {
		if (args.length < 1) {
			return value;
		}
		final List<String> list = asList(args);
		for (int i = value.length - 1; i >= 0; i--) {
			list.add(0, value[i]);
		}
		return list.toArray(new String[0]);
	}

	@Deprecated
	public static String[] removeFirst(String[] args) {
		if (args.length <= 1) {
			return new String[0];
		}
		final List<String> list = asList(args);
		list.remove(0);
		return list.toArray(new String[0]);
	}
}
