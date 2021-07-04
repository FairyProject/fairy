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

package org.fairy.config;

import org.fairy.config.annotation.Comment;
import org.fairy.config.annotation.NestedComment;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Instances of this class contain all comments of a {@link Configuration} class
 * and its fields.
 */
public final class Comments {
    private final List<String> classComments;
    private final Map<String, List<String>> fieldComments;

    private Comments(List<String> classComments,
                     Map<String, List<String>> fieldComments) {
        this.classComments = classComments;
        this.fieldComments = fieldComments;
    }

    static Comments ofClass(Class<?> cls) {
        List<String> classComments = getComments(cls);
        Map<String, List<String>> fieldComments = new HashMap<>();
        Arrays.stream(cls.getDeclaredFields())
                .filter(field -> isCommented(field) || isNestedCommented(field))
                .forEach(field -> {
                    if (isCommented(field)) {
                        fieldComments.put(field.getName(), Comments.getComments(field));
                    }
                    if (isNestedCommented(field)) {
                        putNestedComments(field, fieldComments, "");
                    }
                });
        return new Comments(classComments, fieldComments);
    }

    private static void putNestedComments(Field field, Map<String, List<String>> map, String path) {
        Class<?> type = field.getType();

        if (path.equals("") || path.isEmpty()) {
            path = field.getName() + ".";
        }

        if (type.getDeclaredFields().length > 0) {
            String finalMain = path;
            Arrays.stream(type.getDeclaredFields())
                    .filter(f -> isCommented(f) || isNestedCommented(f))
                    .forEach(f -> {

                        if (isCommented(f)) {
                            map.put(finalMain + f.getName(), Comments.getComments(f));
                        }

                        if (isNestedCommented(f)) {
                            putNestedComments(f, map, finalMain + f.getName() + ".");
                        }

                    });
        }
    }

    private static boolean isCommented(AnnotatedElement element) {
        return element.isAnnotationPresent(Comment.class);
    }

    private static boolean isNestedCommented(AnnotatedElement element) {
        return element.isAnnotationPresent(NestedComment.class);
    }

    private static List<String> getComments(AnnotatedElement element) {
        Comment comment = element.getAnnotation(Comment.class);
        return (comment != null)
                ? Arrays.asList(comment.value())
                : Collections.emptyList();
    }

    /**
     * Returns if the {@code Configuration} this {@code Comments} object belongs to
     * has class comments.
     *
     * @return true, if {@code Configuration} has class comments.
     */
    public boolean hasClassComments() {
        return !classComments.isEmpty();
    }

    /**
     * Returns if the {@code Configuration} this {@code Comments} object belongs to
     * has field comments.
     *
     * @return true, if {@code Configuration} has field comments.
     */
    public boolean hasFieldComments() {
        return !fieldComments.isEmpty();
    }

    /**
     * Returns a list of class comments.
     *
     * @return list of class comments
     */
    public List<String> getClassComments() {
        return classComments;
    }

    /**
     * Returns lists of field comments mapped by field name.
     *
     * @return lists of field comments by field name
     */
    public Map<String, List<String>> getFieldComments() {
        return fieldComments;
    }
}
