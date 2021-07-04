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

package org.fairy.bukkit.impl.test;

import org.fairy.bukkit.impl.annotation.ProviderTestImpl;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImplementationFactory {

    private static final Map<Class<? extends ImplementationTest>, TestResult> TESTS = new ConcurrentHashMap<>();

    public static TestResult test(@Nullable ProviderTestImpl testAnnotation) {
        if (testAnnotation == null) {
            return TestResult.NO_PROVIDER;
        }

        Class<? extends ImplementationTest> type = testAnnotation.value();
        if (TESTS.containsKey(type)) {
            return TESTS.get(type);
        }

        try {
            ImplementationTest test = type.newInstance();

            TestResult result = test.test() ? TestResult.SUCCESS : TestResult.FAILURE;
            TESTS.put(type, result);

            return result;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public enum TestResult {

        SUCCESS,
        FAILURE,
        NO_PROVIDER

    }

}
