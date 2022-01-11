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

package io.fairyproject.bukkit.reflection.annotation;

import io.fairyproject.bukkit.reflection.minecraft.MinecraftVersion;
import io.fairyproject.bukkit.reflection.resolver.ClassResolver;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.wrapper.ClassWrapper;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionAnnotations {

	public static final ReflectionAnnotations INSTANCE = new ReflectionAnnotations();

	static final Pattern classRefPattern = Pattern.compile("@Class\\((.*)\\)");

	private ReflectionAnnotations() {
	}

	public void load(Object toLoad) {
		if (toLoad == null) { throw new IllegalArgumentException("toLoad cannot be null"); }

		ClassResolver classResolver = new ClassResolver();

		for (java.lang.reflect.Field field : toLoad.getClass().getDeclaredFields()) {
			Class classAnnotation = field.getAnnotation(Class.class);
			Field fieldAnnotation = field.getAnnotation(Field.class);
			Method methodAnnotation = field.getAnnotation(Method.class);

			if (classAnnotation == null && fieldAnnotation == null && methodAnnotation == null) {
				continue;
			} else {
				field.setAccessible(true);
			}

			if (classAnnotation != null) {
				List<String> nameList = parseAnnotationVersions(Class.class, classAnnotation);
				if (nameList.isEmpty()) { throw new IllegalArgumentException("@Class names cannot be empty"); }
				String[] names = nameList.toArray(new String[nameList.size()]);
				for (int i = 0; i < names.length; i++) {// Replace NMS & OBC
					names[i] = names[i]
							.replace("{nms}", "net.minecraft.server." + MinecraftVersion.get().packageName())
							.replace("{obc}", "org.bukkit.craftbukkit." + MinecraftVersion.get().packageName());
				}
				try {
					if (ClassWrapper.class.isAssignableFrom(field.getType())) {
						field.set(toLoad, classResolver.resolveWrapper(names));
					} else if (java.lang.Class.class.isAssignableFrom(field.getType())) {
						field.set(toLoad, classResolver.resolve(names));
					} else {
						throwInvalidFieldType(field, toLoad, "Class or ClassWrapper");
						return;
					}
				} catch (ReflectiveOperationException e) {
					if (!classAnnotation.ignoreExceptions()) {
						throwReflectionException("@Class", field, toLoad, e);
						return;
					}
				}
			} else if (fieldAnnotation != null) {
				List<String> nameList = parseAnnotationVersions(Field.class, fieldAnnotation);
				if (nameList.isEmpty()) { throw new IllegalArgumentException("@Field names cannot be empty"); }
				String[] names = nameList.toArray(new String[nameList.size()]);
				try {
					FieldResolver fieldResolver = new FieldResolver(parseClass(Field.class, fieldAnnotation, toLoad));
					if (FieldWrapper.class.isAssignableFrom(field.getType())) {
						field.set(toLoad, fieldResolver.resolveWrapper(names));
					} else if (java.lang.reflect.Field.class.isAssignableFrom(field.getType())) {
						field.set(toLoad, fieldResolver.resolve(names));
					} else {
						throwInvalidFieldType(field, toLoad, "Field or FieldWrapper");
						return;
					}
				} catch (ReflectiveOperationException e) {
					if (!fieldAnnotation.ignoreExceptions()) {
						throwReflectionException("@Field", field, toLoad, e);
						return;
					}
				}
			} else if (methodAnnotation != null) {
				List<String> nameList = parseAnnotationVersions(Method.class, methodAnnotation);
				if (nameList.isEmpty()) { throw new IllegalArgumentException("@Method names cannot be empty"); }
				String[] names = nameList.toArray(new String[nameList.size()]);

				boolean isSignature = names[0].contains(" ");// Only signatures can contain spaces (e.g. "void aMethod()")
				for (String s : names) {
					if (s.contains(" ") != isSignature) {
						throw new IllegalArgumentException("Inconsistent method names: Cannot have mixed signatures/names");
					}
				}

				try {
					MethodResolver methodResolver = new MethodResolver(parseClass(Method.class, methodAnnotation, toLoad));
					if (MethodWrapper.class.isAssignableFrom(field.getType())) {
						if (isSignature) {
							field.set(toLoad, methodResolver.resolveSignatureWrapper(names));
						} else {
							field.set(toLoad, methodResolver.resolveWrapper(names));
						}
					} else if (java.lang.reflect.Method.class.isAssignableFrom(field.getType())) {
						if (isSignature) {
							field.set(toLoad, methodResolver.resolveSignature(names));
						} else {
							field.set(toLoad, methodResolver.resolve(names));
						}
					} else {
						throwInvalidFieldType(field, toLoad, "Method or MethodWrapper");
						return;
					}
				} catch (ReflectiveOperationException e) {
					if (!methodAnnotation.ignoreExceptions()) {
						throwReflectionException("@Method", field, toLoad, e);
						return;
					}
				}
			}
		}
	}

	/**
	 * Parses an annotation to the current server version. Removes all entries that don't match the version, but keeps the original order for matching names.
	 *
	 * @param clazz      Class of the annotation
	 * @param annotation annotation
	 * @param <A>        annotation type
	 * @return a list of matching names
	 */
	<A extends Annotation> List<String> parseAnnotationVersions(java.lang.Class<A> clazz, A annotation) {
		List<String> list = new ArrayList<>();

		try {
			String[] names = (String[]) clazz.getMethod("value").invoke(annotation);
			MinecraftReflection.Version[] versions = (MinecraftReflection.Version[]) clazz.getMethod("versions").invoke(annotation);

			if (versions.length == 0) {// No versions specified -> directly use the names
				for (String name : names) {
					list.add(name);
				}
			} else {
				if (versions.length > names.length) {
					throw new RuntimeException("versions array cannot have more elements than the names (" + clazz + ")");
				}
				for (int i = 0; i < versions.length; i++) {
					if (MinecraftVersion.get().equal(versions[i])) {// Wohoo, perfect match!
						list.add(names[i]);
					} else {
						if (names[i].startsWith(">") && MinecraftVersion.get().versionEnum().newerThan(versions[i])) {// Match if the current version is newer
							list.add(names[i].substring(1));
						} else if (names[i].startsWith("<") && MinecraftVersion.get().versionEnum().olderThan(versions[i])) {// Match if the current version is older
							list.add(names[i].substring(1));
						}
					}
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

		return list;
	}

	<A extends Annotation> String parseClass(java.lang.Class<A> clazz, A annotation, Object toLoad) {
		try {
			String className = (String) clazz.getMethod("className").invoke(annotation);
			Matcher matcher = classRefPattern.matcher(className);
			while (matcher.find()) {
				if (matcher.groupCount() != 1) { continue; }
				String fieldName = matcher.group(1);// It's a reference to a previously loaded class
				java.lang.reflect.Field field = toLoad.getClass().getField(fieldName);
				if (ClassWrapper.class.isAssignableFrom(field.getType())) {
					return ((ClassWrapper) field.get(toLoad)).getName();
				} else if (java.lang.Class.class.isAssignableFrom(field.getType())) {
					return ((java.lang.Class) field.get(toLoad)).getName();
				}
			}
			return className;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	void throwInvalidFieldType(java.lang.reflect.Field field, Object toLoad, String expected) {
		throw new IllegalArgumentException("Field " + field.getName() + " in " + toLoad.getClass() + " is not of type " + expected + ", it's " + field.getType());
	}

	void throwReflectionException(String annotation, java.lang.reflect.Field field, Object toLoad, ReflectiveOperationException exception) {
		throw new RuntimeException("Failed to set " + annotation + " field " + field.getName() + " in " + toLoad.getClass(), exception);
	}

}
