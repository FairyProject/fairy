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

import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves the annotated {@link FieldWrapper} or {@link java.lang.reflect.Field} field to the first matching field name.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

	/**
	 * Name of the class to load this field from
	 *
	 * @return name of the class
	 */
	String className();

	/**
	 * Possible names of the field. Use <code>&gt;</code> or <code>&lt;</code> as a name prefix in combination with {@link #versions()} to specify versions newer- or older-than.
	 *
	 * @return names of the field
	 */
	String[] value();

	/**
	 * Specific versions for the names.
	 *
	 * @return Array of versions for the class names
	 */
	OBCVersion[] versions() default {};

	/**
	 * Whether to ignore any reflection exceptions thrown. Defaults to <code>true</code>
	 *
	 * @return whether to ignore exceptions
	 */
	boolean ignoreExceptions() default true;
}
