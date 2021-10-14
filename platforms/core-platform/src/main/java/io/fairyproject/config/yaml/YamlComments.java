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

package io.fairyproject.config.yaml;

import io.fairyproject.config.Comments;
import io.fairyproject.config.format.FieldNameFormatter;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

final class YamlComments {
    private final Comments comments;

    YamlComments(Comments comments) {
        this.comments = comments;
    }

    String classCommentsAsString() {
        List<String> classComments = comments.getClassComments();
        return commentListToString(classComments);
    }

    Map<String, String> fieldCommentAsStrings(FieldNameFormatter formatter) {
        Map<String, List<String>> fieldComments = comments.getFieldComments();
        return fieldComments.entrySet().stream()
                .map(e -> toFormattedStringCommentEntry(e, formatter))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, String> toFormattedStringCommentEntry(
            Map.Entry<String, List<String>> entry, FieldNameFormatter formatter
    ) {
        String fieldComments = commentListToString(entry.getValue());
        String formattedKey = formatter.fromFieldName(entry.getKey());
        return new MapEntry<>(formattedKey, fieldComments);
    }

    private String commentListToString(List<String> comments) {
        return comments.stream()
                .map(this::toCommentLine)
                .collect(joining("\n"));
    }

    private String toCommentLine(String comment) {
        return comment.isEmpty() ? "" : "# " + comment;
    }

    private static final class MapEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V value;

        public MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }

}
