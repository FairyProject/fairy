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

package io.fairyproject.config;

import io.fairyproject.config.annotation.Format;
import io.fairyproject.config.filter.FieldFilter;
import io.fairyproject.config.format.FieldNameFormatter;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

enum FieldMapper {
    ;

    static Map<String, Object> instanceToMap(Object inst, MappingInfo mappingInfo) {
        Map<String, Object> map = new LinkedHashMap<>();
        Configuration.Properties props = mappingInfo.getProperties();
        FieldFilter filter = props.getFilter();
        for (Field field : filter.filterDeclaredFieldsOf(inst.getClass())) {
            if (field.getAnnotation(Transient.class) != null) {
                continue;
            }
            Object val = toConvertibleObject(field, inst, mappingInfo);
            FieldNameFormatter fnf = selectFormatter(mappingInfo);
            String fn = fnf.fromFieldName(field.getName());
            map.put(fn, val);
        }
        return map;
    }

    private static Object toConvertibleObject(
            Field field, Object instance, MappingInfo mappingInfo
    ) {
        checkDefaultValueNull(field, instance);
        Converter.ConversionInfo info = Converter.ConversionInfo.from(field, instance, mappingInfo);
        Validator.checkFieldWithElementTypeIsContainer(info);
        Object converted = Converters.convertTo(info);
        Validator.checkConverterNotReturnsNull(converted, info);
        return converted;
    }

    static void instanceFromMap(
            Object inst, Map<String, Object> instMap, MappingInfo mappingInfo
    ) {
        FieldFilter filter = mappingInfo.getProperties().getFilter();
        for (Field field : filter.filterDeclaredFieldsOf(inst.getClass())) {
            if (field.getAnnotation(Transient.class) != null) {
                continue;
            }
            FieldNameFormatter fnf = selectFormatter(mappingInfo);
            String fn = fnf.fromFieldName(field.getName());
            Object mapValue = instMap.get(fn);
            if (mapValue != null) {
                fromConvertedObject(field, inst, mapValue, mappingInfo);
            }
        }
    }

    private static void fromConvertedObject(
            Field field, Object instance, Object mapValue,
            MappingInfo mappingInfo
    ) {
        checkDefaultValueNull(field, instance);
        Converter.ConversionInfo info = Converter.ConversionInfo.from(
                field, instance, mapValue, mappingInfo
        );
        Validator.checkFieldWithElementTypeIsContainer(info);
        Object convert = Converters.convertFrom(info);

        if (convert == null) {
            return;
        }

        if (Reflect.isContainerType(info.getFieldType())) {
            Validator.checkFieldTypeAssignableFrom(convert.getClass(), info);
        }

        Reflect.setValue(field, instance, convert);
    }

    private static void checkDefaultValueNull(Field field, Object instance) {
        Object val = Reflect.getValue(field, instance);
        Validator.checkNotNull(val, field.getName());
    }

    static FieldNameFormatter selectFormatter(MappingInfo info) {
        Configuration<?> configuration = info.getConfiguration();
        Configuration.Properties props = info.getProperties();
        if ((configuration != null) &&
                Reflect.hasFormatter(configuration.getClass())) {
            Format format = configuration.getClass()
                    .getAnnotation(Format.class);
            return (format.formatterClass() != FieldNameFormatter.class)
                    ? Reflect.newInstance(format.formatterClass())
                    : format.value();
        }
        return props.getFormatter();
    }

    static final class MappingInfo {
        private final Configuration<?> configuration;
        private final Configuration.Properties properties;

        MappingInfo(
                Configuration<?> configuration,
                Configuration.Properties properties
        ) {
            this.configuration = configuration;
            this.properties = properties;
        }

        Configuration<?> getConfiguration() {
            return configuration;
        }

        Configuration.Properties getProperties() {
            return properties;
        }

        static MappingInfo from(Configuration<?> configuration) {
            return new MappingInfo(configuration, configuration.getProperties());
        }
    }
}
