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

package org.fairy.cache;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CacheUtil {

    private static final ConcurrentHashMap<Class<?>, Field[]> FIELDS_CAHCE = new ConcurrentHashMap<>();
    private static final String SPLIT_STRING = "_";

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).length() == 0;
        }
        Class cl = obj.getClass();
        if (cl.isArray()) {
            int len = Array.getLength(obj);
            return len == 0;
        }
        if (obj instanceof Collection) {
            Collection tempCol = (Collection) obj;
            return tempCol.isEmpty();
        }
        if (obj instanceof Map) {
            Map tempMap = (Map) obj;
            return tempMap.isEmpty();
        }
        if (obj instanceof Optional) {
            return !((Optional) obj).isPresent();
        }
        return false;
    }

    private static int getHashCode(String buf) {
        int hash = 5381;
        int len = buf.length();

        while (len-- > 0) {
            hash = ((hash << 5) + hash) + buf.charAt(len);
        }
        return hash;
    }

    public static String getUniqueHashString(Object obj) {
        return getMiscHashCode(toString(obj));
    }

    public static boolean isPrimitive(Object obj) {
        return obj.getClass().isPrimitive() || obj instanceof String || obj instanceof Integer
                || obj instanceof Long || obj instanceof Byte || obj instanceof Character || obj instanceof Boolean
                || obj instanceof Short || obj instanceof Float || obj instanceof Double || obj instanceof BigDecimal
                || obj instanceof BigInteger;
    }

    public static String toString(Object object) {
        if (object == null) {
            return "null";
        }
        Class<?> type = object.getClass();

        if (isPrimitive(object)) {

            return String.valueOf(object);

        } else if (object instanceof Enum) {

            return ((Enum) object).name();

        } else if (object instanceof Date) {

            return String.valueOf(((Date) object).getTime());

        } else if (object instanceof Calendar) {

            return String.valueOf(((Calendar) object).getTime().getTime());

        } else if (type.isArray()) {

            StringBuilder r = new StringBuilder("[");

            int len = Array.getLength(object);

            for (int i = 0; i < len; i++) {

                if (i > 0) {

                    r.append(",");

                }

                Object val = Array.get(object, i);

                r.append(toString(val));

            }

            return r + "]";

        } else if (object instanceof Iterable<?>) {

            Iterable<?> iterable = (Iterable<?>) object;
            Iterator<?> it = iterable.iterator();

            StringBuilder result = new StringBuilder("[");

            for (int i = 0; it.hasNext(); i++) {
                if (i > 0) {
                    result.append(",");
                }
                Object val = it.next();
                result.append(toString(val));
            }

            return result + "]";

        } else if (object instanceof Map) {
            Map<?, ?> tempMap = (Map<?, ?>) object;

            StringBuilder result = new StringBuilder("{");

            Iterator it = tempMap.entrySet().iterator();

            while (it.hasNext()) {

                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();

                Object key = entry.getKey();

                result.append(toString(key));
                result.append("=");

                Object val = entry.getValue();

                result.append(toString(val));
                if (it.hasNext()) {
                    result.append(",");
                }

            }
            return result + "}";

        } else if (object instanceof Class) {

            Class<?> tmpCls = (Class<?>) object;
            return tmpCls.getName();

        }

        StringBuilder result = new StringBuilder(type.getName());

        do {

            Field[] fields = FIELDS_CAHCE.get(type);

            if (fields == null) {

                fields = type.getDeclaredFields();

                AccessibleObject.setAccessible(fields, true);
                FIELDS_CAHCE.put(type, fields);

            }

            if (fields.length == 0) {
                type = type.getSuperclass();
                continue;
            }

            result.append("[");

            // get the names and values of all fields
            for (Field f : fields) {

                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                if (f.isSynthetic() || f.getName().contains("this$")) {
                    continue;
                }

                result.append(f.getName()).append("=");

                try {
                    Object val = f.get(object);
                    result.append(toString(val));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                result.append(",");


            }

            String comma = ",";

            if (result.toString().endsWith(comma)) {
                result = new StringBuilder(result.substring(0, result.length() - 1));
            }

            result.append("]");
            type = type.getSuperclass();

        } while (type != null);

        return result.toString();
    }

    /**
     * 通过混合Hash算法，将长字符串转为短字符串（字符串长度小于等于20时，不做处理）
     *
     * @param str String
     * @return Hash字符串
     */
    public static String getMiscHashCode(String str) {
        if (null == str || str.length() == 0) {
            return "";
        }
        int originSize = 20;
        if (str.length() <= originSize) {
            return str;
        }
        StringBuilder tmp = new StringBuilder();
        tmp.append(str.hashCode()).append(SPLIT_STRING).append(getHashCode(str));

        int mid = str.length() / 2;
        String str1 = str.substring(0, mid);
        String str2 = str.substring(mid);
        tmp.append(SPLIT_STRING).append(str1.hashCode());
        tmp.append(SPLIT_STRING).append(str2.hashCode());

        return tmp.toString();
    }

    /**
     * 生成缓存Key
     *
     * @param className 类名称
     * @param method    方法名称
     * @param arguments 参数
     * @return CacheKey 缓存Key
     */
    public static String getDefaultCacheKey(String className, String method, Object[] arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append(getDefaultCacheKeyPrefix(className, method, arguments));
        if (null != arguments && arguments.length > 0) {
            sb.append(getUniqueHashString(arguments));
        }
        return sb.toString();
    }

    /**
     * 生成缓存Key的前缀
     *
     * @param className 类名称
     * @param method    方法名称
     * @param arguments 参数
     * @return CacheKey 缓存Key
     */
    public static String getDefaultCacheKeyPrefix(String className, String method, Object[] arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        if (null != method && method.length() > 0) {
            sb.append(".").append(method);
        }
        return sb.toString();
    }

}
