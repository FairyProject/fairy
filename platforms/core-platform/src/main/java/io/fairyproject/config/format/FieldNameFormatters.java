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

package io.fairyproject.config.format;

public enum FieldNameFormatters implements FieldNameFormatter {
    /**
     * Represents a {@code FieldNameFormatter} that doesn't actually format the
     * field name but instead returns it.
     */
    IDENTITY {
        @Override
        public String fromFieldName(String fn) {
            return fn;
        }
    },
    /**
     * Represents a {@code FieldNameFormatter} that transforms <i>camelCase</i> to
     * <i>lower_underscore</i>.
     * <p>
     * For example, <i>myPrivateField</i> becomes <i>my_private_field</i>.
     */
    LOWER_UNDERSCORE {
        @Override
        public String fromFieldName(String fn) {
            StringBuilder builder = new StringBuilder(fn.length());
            for (char c : fn.toCharArray()) {
                if (Character.isLowerCase(c)) {
                    builder.append(c);
                } else if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                    builder.append('_').append(c);
                }
            }
            return builder.toString();
        }
    },

    LOWER_CASE {
        @Override
        public String fromFieldName(String fn) {
            return fn.toLowerCase();
        }
    },
    /**
     * Represents a {@code FieldNameFormatter} that transforms <i>camelCase</i> to
     * <i>UPPER_UNDERSCORE</i>.
     * <p>
     * For example, <i>myPrivateField</i> becomes <i>MY_PRIVATE_FIELD</i>.
     */
    UPPER_UNDERSCORE {
        @Override
        public String fromFieldName(String fieldName) {
            StringBuilder builder = new StringBuilder(fieldName.length());
            for (char c : fieldName.toCharArray()) {
                if (Character.isLowerCase(c)) {
                    builder.append(Character.toUpperCase(c));
                } else if (Character.isUpperCase(c)) {
                    builder.append('_').append(c);
                }
            }
            return builder.toString();
        }
    }
}
