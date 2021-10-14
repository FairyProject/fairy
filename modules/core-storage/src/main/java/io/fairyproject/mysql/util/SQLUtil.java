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

package io.fairyproject.mysql.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class SQLUtil {

    public static String join(String[] strs) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(strs[i]);
        }
        return buf.toString();
    }

    public static String join(Collection<String> strs) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String col : strs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(col);
        }
        return sb.toString();
    }

    public static String getQuestionMarks(int count) {
        StringBuilder sb = new StringBuilder(count * 2);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }

    public static boolean isPrimitiveOrString(Class<?> c) {
        if (c.isPrimitive()) {
            return true;
        } else if (c == Byte.class || c == Short.class || c == Integer.class || c == Long.class || c == Float.class
                || c == Double.class || c == Boolean.class || c == Character.class || c == String.class) {
            return true;
        } else {
            return false;
        }
    }

    public static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == void.class) {
            return Void.class;
        }
        throw new RuntimeException("Will never get here");
    }

}
