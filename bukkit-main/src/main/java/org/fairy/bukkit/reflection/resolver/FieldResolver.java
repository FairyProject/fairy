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

package org.fairy.bukkit.reflection.resolver;

import org.fairy.bukkit.reflection.wrapper.FieldWrapper;
import org.fairy.util.AccessUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolver for fields
 */
public class FieldResolver extends MemberResolver<Field> {

	public FieldResolver(Class<?> clazz) {
		super(clazz);
	}

	public FieldResolver(String className) throws ClassNotFoundException {
		super(className);
	}

	@Override
	public Field resolveIndex(int index) throws IndexOutOfBoundsException, ReflectiveOperationException {
		return AccessUtil.setAccessible(this.clazz.getDeclaredFields()[index]);
	}

	@Override
	public Field resolveIndexSilent(int index) {
		try {
			return resolveIndex(index);
		} catch (IndexOutOfBoundsException | ReflectiveOperationException ignored) {
		}
		return null;
	}

	@Override
	public FieldWrapper resolveIndexWrapper(int index) {
		return new FieldWrapper<>(resolveIndexSilent(index));
	}

	public FieldWrapper resolveWrapper(String... names) {
		return new FieldWrapper<>(resolveSilent(names));
	}

	public Field resolveSilent(String... names) {
		try {
			return resolve(names);
		} catch (Exception e) {
		}
		return null;
	}

	public Field resolve(String... names) throws NoSuchFieldException {
		ResolverQuery.Builder builder = ResolverQuery.builder();
		for (String name : names)
			builder.with(name);
		try {
			return super.resolve(builder.build());
		} catch (ReflectiveOperationException e) {
			throw (NoSuchFieldException) e;
		}
	}

	public Field resolveSilent(ResolverQuery... queries) {
		try {
			return resolve(queries);
		} catch (Exception e) {
		}
		return null;
	}

	public Field resolve(ResolverQuery... queries) throws NoSuchFieldException {
		try {
			return super.resolve(queries);
		} catch (ReflectiveOperationException e) {
			throw (NoSuchFieldException) e;
		}
	}

	public <T> FieldWrapper<T> resolve(Class<T> type, int index) {
		try {
			return new FieldWrapper<>(this.resolve(new ResolverQuery(type, index)));
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public <T> FieldWrapper<T> resolveSilent(Class<T> type, int index) {
		return new FieldWrapper<>(this.resolveSilent(new ResolverQuery(type, index)));
	}

	public <T> List<FieldWrapper<T>> resolveList(Class<T> type) {
		List<FieldWrapper<T>> fieldList = new ArrayList<>();

		try {
			int index = 0;
			while (true) {
				FieldWrapper<T> field;
				try {
					field = this.resolve(type, index++);
				} catch (IllegalArgumentException e) {
					break;
				}

				fieldList.add(field);
			}
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}

		return fieldList;
	}

	public <T> FieldWrapper<T> resolveWithGenericType(Class<T> fieldType, Class<?>... genericType) {
		try {
			return new FieldWrapper<>(this.resolve(new ResolverQuery(fieldType, -1, genericType)));
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	@Override
	protected Field resolveObject(ResolverQuery query) throws ReflectiveOperationException {
		return this.accessorCache.resolveField(query);
	}

	/**
	 * Attempts to find the first field of the specified type
	 *
	 * @param type Type to find
	 * @return the Field
	 * @throws ReflectiveOperationException (usually never)
	 * @see #resolveByLastType(Class)
	 */
	public Field resolveByFirstType(Class<?> type) throws ReflectiveOperationException {
		return this.resolve(new ResolverQuery(type, 0));
	}

	public FieldWrapper resolveByFirstTypeWrapper(Class<?> type) throws ReflectiveOperationException {
		return new FieldWrapper(this.resolveByFirstType(type));
	}

	public FieldWrapper resolveByFirstTypeDynamic(Class<?> type) throws ReflectiveOperationException {
		Field field = this.resolve(new ResolverQuery(type, -1).withModifierOptions(ResolverQuery.ModifierOptions.builder()
				.onlyDynamic(true)
				.build()));

		if (field != null)
			return new FieldWrapper<>(field);
		throw new NoSuchFieldException("Could not resolve field of type '" + type.toString() + "' in class " + this.clazz);
	}

	/**
	 * Attempts to find the first field of the specified type
	 *
	 * @param type Type to find
	 * @return the Field
	 * @see #resolveByLastTypeSilent(Class)
	 */
	public Field resolveByFirstTypeSilent(Class<?> type) {
		try {
			return resolveByFirstType(type);
		} catch (Exception e) {
		}
		return null;
	}


	/**
	 * Attempts to find the last field of the specified type
	 *
	 * @param type Type to find
	 * @return the Field
	 * @throws ReflectiveOperationException (usually never)
	 * @see #resolveByFirstType(Class)
	 */
	public Field resolveByLastType(Class<?> type) throws ReflectiveOperationException {
		Field field = this.resolve(new ResolverQuery(type, -2));
		if (field == null) { throw new NoSuchFieldException("Could not resolve field of type '" + type.toString() + "' in class " + this.clazz); }
		return field;
	}

	public FieldWrapper resolveByLastTypeWrapper(Class<?> type) throws ReflectiveOperationException {
		return new FieldWrapper(this.resolveByLastType(type));
	}

	public Field resolveByLastTypeSilent(Class<?> type) {
		try {
			return resolveByLastType(type);
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	protected NoSuchFieldException notFoundException(String joinedNames) {
		return new NoSuchFieldException("Could not resolve field for " + joinedNames + " in class " + this.clazz);
	}
}
