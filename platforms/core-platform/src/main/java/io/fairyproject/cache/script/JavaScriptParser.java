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

import io.fairyproject.cache.CacheUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Language;

import javax.script.*;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class JavaScriptParser extends AbstractScriptParser {

    private static final Logger LOGGER = LogManager.getLogger(JavaScriptParser.class);

    private final ScriptEngineManager engineManager = new ScriptEngineManager();

    private final ConcurrentHashMap<String, CompiledScript> expressenCache = new ConcurrentHashMap<>();
    private final StringBuilder functions = new StringBuilder();

    private final ScriptEngine engine;

    public JavaScriptParser() {
        this.engine = this.engineManager.getEngineByName("javascript");

        try {
            this.addFunction(HASH, CacheUtil.class.getDeclaredMethod("getUniqueHashString", Object.class));
            this.addFunction(EMPTY, CacheUtil.class.getDeclaredMethod("isEmpty", Object.class));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void addFunction(String name, Method method) {
        try {
            String clsName = method.getDeclaringClass().getName();
            String methodName = method.getName();
            functions.append("function ")
                    .append(name)
                    .append("(obj){return ")
                    .append(clsName)
                    .append(".")
                    .append(methodName)
                    .append("(obj);}");
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public <T> T getElValue(@Language("JavaScript") String exp, Object target, Object[] arguments, Object retVal, boolean hasRetVal, Class<T> valueType) throws Exception {
        Bindings bindings = new SimpleBindings();
        bindings.put(TARGET, target);
        bindings.put(ARGS, arguments);
        if (hasRetVal) {
            bindings.put(RET_VAL, retVal);
        }
        CompiledScript script = this.expressenCache.get(exp);
        if (null != script) {
            return (T) script.eval(bindings);
        }
        if (engine instanceof Compilable) {
            Compilable compEngine = (Compilable) engine;
            script = compEngine.compile(functions + exp);
            expressenCache.put(exp, script);
            return (T) script.eval(bindings);
        } else {
            return (T) engine.eval(functions + exp, bindings);
        }
    }
}
