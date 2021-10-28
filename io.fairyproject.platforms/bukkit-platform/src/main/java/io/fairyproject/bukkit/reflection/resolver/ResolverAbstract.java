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

import java.util.Arrays;

/**
 * Abstract resolver class
 *
 * @param <T> resolved type
 * @see ClassResolver
 * @see ConstructorResolver
 * @see FieldResolver
 * @see MethodResolver
 */
public abstract class ResolverAbstract<T> {

	/**
	 * Same as {@link #resolve(ResolverQuery...)} but throws no exceptions
	 *
	 * @param queries Array of possible queries
	 * @return the resolved object if it was found, <code>null</code> otherwise
	 */
	protected T resolveSilent(ResolverQuery... queries) {
		try {
			return resolve(queries);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Attempts to resolve an array of possible queries to an object
	 *
	 * @param queries Array of possible queries
	 * @return the resolved object (if it was found)
	 * @throws ReflectiveOperationException if none of the possibilities could be resolved
	 * @throws IllegalArgumentException     if the given possibilities are empty
	 */
	protected T resolve(ResolverQuery... queries) throws ReflectiveOperationException {
		if (queries == null || queries.length <= 0) { throw new IllegalArgumentException("Given possibilities are empty"); }
		for (ResolverQuery query : queries) {
			try {
				return resolveObject(query);
			} catch (ReflectiveOperationException e) {
			}
		}

		//Couldn't find any of the possibilities
		throw notFoundException(Arrays.asList(queries).toString());
	}

	protected abstract T resolveObject(ResolverQuery query) throws ReflectiveOperationException;

	protected ReflectiveOperationException notFoundException(String joinedNames) {
		return new ReflectiveOperationException("Objects could not be resolved: " + joinedNames);
	}

}
