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

package io.fairyproject.bukkit.reflection.resolver;

import io.fairyproject.bukkit.reflection.accessor.ClassAccessorCache;
import io.fairyproject.bukkit.reflection.wrapper.WrapperAbstract;

import java.lang.reflect.Member;

/**
 * abstract class to resolve members
 *
 * @param <T> member type
 * @see ConstructorResolver
 * @see FieldResolver
 * @see MethodResolver
 */
public abstract class MemberResolver<T extends Member> extends ResolverAbstract<T> {

	protected Class<?> clazz;
	protected ClassAccessorCache accessorCache;

	public MemberResolver(Class<?> clazz, ClassAccessorCache accessorCache) {
		if (clazz == null) { throw new IllegalArgumentException("class cannot be null"); }
		this.clazz = clazz;
		this.accessorCache = accessorCache;
	}

	public MemberResolver(Class<?> clazz) {
		this(clazz, ClassAccessorCache.get(clazz));
	}

	public MemberResolver(String className) throws ClassNotFoundException {
		this(new ClassResolver().resolve(className));
	}

	/**
	 * Resolve a member by its index
	 *
	 * @param index index
	 * @return the member
	 * @throws IndexOutOfBoundsException    if the specified index is out of the available member bounds
	 * @throws ReflectiveOperationException if the object could not be set accessible
	 */
	public abstract T resolveIndex(int index) throws IndexOutOfBoundsException, ReflectiveOperationException;

	/**
	 * Resolve member by its index (without exceptions)
	 *
	 * @param index index
	 * @return the member or <code>null</code>
	 */
	public abstract T resolveIndexSilent(int index);

	/**
	 * Resolce member wrapper by its index
	 *
	 * @param index index
	 * @return the wrapped member
	 */
	public abstract WrapperAbstract resolveIndexWrapper(int index);

}
