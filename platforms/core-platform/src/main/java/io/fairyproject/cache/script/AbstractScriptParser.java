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

package io.fairyproject.cache.script;

import java.lang.reflect.Method;

public abstract class AbstractScriptParser {

    protected static final String TARGET = "target";

    protected static final String ARGS = "args";

    protected static final String RET_VAL = "retVal";

    protected static final String HASH = "hash";

    protected static final String EMPTY = "empty";

    public String getDefinedCacheKey(String keyEL, Object target, Object[] arguments, Object retVal, boolean hasRetVal)
            throws Exception {
        return this.getElValue(keyEL, target, arguments, retVal, hasRetVal, String.class);
    }

    public <T> T getElValue(String keyEL, Object target, Object[] arguments, Class<T> valueType) throws Exception {
        return this.getElValue(keyEL, target, arguments, null, false, valueType);
    }

    public abstract <T> T getElValue(String exp, Object target, Object[] arguments, Object retVal, boolean hasRetVal,
                                     Class<T> valueType) throws Exception;

    public abstract void addFunction(String name, Method method);

}
