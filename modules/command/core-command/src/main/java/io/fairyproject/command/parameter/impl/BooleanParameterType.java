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

package io.fairyproject.command.parameter.impl;

import com.google.common.collect.ImmutableMap;
import io.fairyproject.bean.Component;
import io.fairyproject.command.CommandContext;
import io.fairyproject.command.MessageType;
import io.fairyproject.command.parameter.ParameterHolder;

import java.util.Map;

@Component
public class BooleanParameterType implements ParameterHolder<Boolean> {

    private static final Map<String, Boolean> MAP;

    static {
        MAP = ImmutableMap.<String, Boolean>builder()
                .put("true", true)
                .put("on", true)
                .put("yes", true)
                .put("false", false)
                .put("off", false)
                .put("no", false)
        .build();
    }

    @Override
    public Class[] type() {
        return new Class[] {Boolean.class, boolean.class};
    }

    public Boolean transform(CommandContext event, String source) {
        if (!MAP.containsKey(source.toLowerCase())) {
            event.sendMessage(MessageType.WARN, source + " is not a valid boolean.");
            return (null);
        }

        return MAP.get(source.toLowerCase());
    }

}