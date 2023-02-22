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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Default {@link ClassResolver}
 */
public class ClassResolver extends ResolverAbstract<Class<?>> {

	private static final LoadingCache<String, Optional<Class<?>>> CLASS_CACHE = CacheBuilder
			.newBuilder()
			.expireAfterAccess(1L, TimeUnit.MINUTES)
			.build(new CacheLoader<String, Optional<Class<?>>>() {
				@Override
				public @NotNull Optional<Class<?>> load(@NotNull String s) {
					try {
						return Optional.of(Class.forName(s));
					} catch (ClassNotFoundException ex) {
						return Optional.empty();
					}
				}
			});

	public void cache(String name, Class<?> type) {
		CLASS_CACHE.put(name, Optional.of(type));
	}

	public Class<?> resolveSilent(String... names) {
		try {
			return resolve(names);
		} catch (Exception e) {
		}
		return null;
	}

	public Class<?> resolve(String... names) throws ClassNotFoundException {
		ResolverQuery.Builder builder = ResolverQuery.builder();
		for (String name : names)
			builder.with(name);
		try {
			return super.resolve(builder.build());
		} catch (ReflectiveOperationException e) {
			throw (ClassNotFoundException) e;
		}
	}

	public Class<?> resolveSubClass(Class<?> mainClass, String... names) throws ClassNotFoundException {
		ResolverQuery.Builder builder = ResolverQuery.builder();
		String prefix = mainClass.getName() + "$";
		for (String name : names)
			builder.with(prefix + name);
		try {
			return super.resolve(builder.build());
		} catch (ReflectiveOperationException e) {
			throw (ClassNotFoundException) e;
		}
	}

	@Override
	protected Class<?> resolveObject(ResolverQuery query) throws ReflectiveOperationException {
		Class<?> result = ThrowingSupplier.sneaky(() -> CLASS_CACHE.get(query.getName())).get().orElse(null);

		if (result == null) {
			throw new ClassNotFoundException(query.toString());
		}

		return result;
	}

	@Override
	protected ClassNotFoundException notFoundException(String joinedNames) {
		return new ClassNotFoundException("Could not resolve class for " + joinedNames);
	}
}
