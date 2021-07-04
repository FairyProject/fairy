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

import org.fairy.bukkit.reflection.wrapper.MethodWrapper;
import org.fairy.util.AccessUtil;

import java.lang.reflect.Method;

/**
 * Resolver for methods
 *
 * @credit inventivetalent
 * @modified LeeGod
 *
 */
public class MethodResolver extends MemberResolver<Method> {

	public MethodResolver(Class<?> clazz) {
		super(clazz);
	}

	public MethodResolver(String className) throws ClassNotFoundException {
		super(className);
	}

	public Method resolveSignature(String... signatures)throws ReflectiveOperationException {
		for (Method method : clazz.getDeclaredMethods()) {
			String methodSignature = MethodWrapper.getMethodSignature(method);
			for (String s : signatures) {
				if (s.equals(methodSignature)) {
					return AccessUtil.setAccessible(method);
				}
			}
		}
		return null;
	}

	public Method resolveSignatureSilent(String... signatures) {
		try {
			return resolveSignature(signatures);
		} catch (ReflectiveOperationException ignored) {
		}
		return null;
	}

	public MethodWrapper resolveSignatureWrapper(String... signatures) {
		return new MethodWrapper(resolveSignatureSilent(signatures));
	}

	public MethodWrapper resolve(int index, Class<?>... parameters) throws ReflectiveOperationException {

		return new MethodWrapper(this.resolve(new ResolverQuery(index, parameters)));

	}

	public MethodWrapper resolve(Class<?> returnType, int index, Class<?>... parameters) throws ReflectiveOperationException {

		return new MethodWrapper<>(this.resolve(new ResolverQuery(returnType, index, parameters)));

	}

	@Override
	public Method resolveIndex(int index) throws IndexOutOfBoundsException, ReflectiveOperationException {
		return AccessUtil.setAccessible(this.clazz.getDeclaredMethods()[index]);
	}

	@Override
	public Method resolveIndexSilent(int index) {
		try {
			return resolveIndex(index);
		} catch (IndexOutOfBoundsException | ReflectiveOperationException ignored) {
		}
		return null;
	}

	@Override
	public MethodWrapper resolveIndexWrapper(int index) {
		return new MethodWrapper<>(resolveIndexSilent(index));
	}

	public MethodWrapper resolveWrapper(String... names) {
		return new MethodWrapper<>(resolveSilent(names));
	}

	public MethodWrapper resolveWrapper(ResolverQuery... queries) {
		return new MethodWrapper<>(resolveSilent(queries));
	}

	public Method resolveSilent(String... names) {
		try {
			return resolve(names);
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public Method resolveSilent(ResolverQuery... queries) {
		return super.resolveSilent(queries);
	}

	public Method resolve(String... names) throws NoSuchMethodException {
		ResolverQuery.Builder builder = ResolverQuery.builder();
		for (String name : names) {
			builder.with(name);
		}
		return resolve(builder.build());
	}

	@Override
	public Method resolve(ResolverQuery... queries) throws NoSuchMethodException {
		try {
			return super.resolve(queries);
		} catch (ReflectiveOperationException e) {
			throw (NoSuchMethodException) e;
		}
	}

	@Override
	protected Method resolveObject(ResolverQuery query) throws ReflectiveOperationException {
		return this.accessorCache.resolveMethod(query);
	}

	@Override
	protected NoSuchMethodException notFoundException(String joinedNames) {
		return new NoSuchMethodException("Could not resolve method for " + joinedNames + " in class " + this.clazz);
	}
}
