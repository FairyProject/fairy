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

import io.fairyproject.config.annotation.Convert;
import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.SerializerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

final class Converters {
    @Autowired
    private static SerializerFactory SERIALIZER_FACTORY;

    private static final Map<Class<? extends Converter<?, ?>>, Converter<?, ?>> cache
            = new WeakHashMap<>();
    static final IdentityConverter IDENTITY_CONVERTER
            = new IdentityConverter();
    static final SimpleTypeConverter SIMPLE_TYPE_CONVERTER
            = new SimpleTypeConverter();
    static final EnumConverter ENUM_CONVERTER
            = new EnumConverter();
    static final ListConverter LIST_CONVERTER
            = new ListConverter();
    static final SetConverter SET_CONVERTER
            = new SetConverter();
    static final MapConverter MAP_CONVERTER
            = new MapConverter();
    static final SimpleListConverter SIMPLE_LIST_CONVERTER
            = new SimpleListConverter();
    static final SimpleSetConverter SIMPLE_SET_CONVERTER
            = new SimpleSetConverter();
    static final SimpleMapConverter SIMPLE_MAP_CONVERTER
            = new SimpleMapConverter();
    static final ConfigurationElementConverter ELEMENT_CONVERTER
            = new ConfigurationElementConverter();

    static Object convertTo(Converter.ConversionInfo info) {
        Converter<Object, Object> converter = selectConverter(
                info.getValueType(), info
        );
        converter.preConvertTo(info);
        return tryConvertTo(converter, info);
    }

    private static Object tryConvertTo(
            Converter<Object, Object> converter, Converter.ConversionInfo info
    ) {
        try {
            return converter.convertTo(info.getValue(), info);
        } catch (ClassCastException e) {
            String msg = "Converter '" + converter.getClass().getSimpleName() + "'" +
                    " cannot convert value '" + info.getValue() + "' of field '" +
                    info.getFieldName() + "' because it expects a different type.";
            throw new ConfigurationException(msg, e);
        }
    }

    static Object convertFrom(Converter.ConversionInfo info) {
        Converter<Object, Object> converter = selectConverter(
                info.getValueType(), info
        );
        converter.preConvertFrom(info);
        return tryConvertFrom(converter, info);
    }

    private static Object tryConvertFrom(
            Converter<Object, Object> converter, Converter.ConversionInfo info
    ) {
        try {
            return converter.convertFrom(info.getMapValue(), info);
        } catch (ClassCastException | IllegalArgumentException e) {
            String msg = "The value for field '" + info.getFieldName() + "' with " +
                    "type '" + getClsName(info.getFieldType()) + "' cannot " +
                    "be converted back to its original representation because a " +
                    "type mismatch occurred.";
            throw new ConfigurationException(msg, e);
        }
    }

    private static String getClsName(Class<?> cls) {
        return cls.getSimpleName();
    }

    private static Converter<Object, Object> selectConverter(
            Class<?> valueType, Converter.ConversionInfo info
    ) {
        Converter<?, ?> converter;

        if (Reflect.hasNoConvert(info.getField())) {
            converter = IDENTITY_CONVERTER;
        } else if (Reflect.hasConverter(info.getField())) {
            converter = instantiateConverter(info.getField());
        } else if (Reflect.isSimpleType(valueType)) {
            converter = SIMPLE_TYPE_CONVERTER;
        } else {
            converter = selectNonSimpleConverter(valueType, info);
        }
        return toObjectConverter(converter);
    }

    private static Converter<Object, Object> selectNonSimpleConverter(
            Class<?> valueType, Converter.ConversionInfo info
    ) {
        if (SERIALIZER_FACTORY != null) {
            ObjectSerializer<?, ?> serializer = SERIALIZER_FACTORY.findSerializer(valueType);

            if (serializer != null) {
                return new SerializerConverter(serializer);
            }
        }

        Converter<?, ?> converter;
        if (Reflect.isEnumType(valueType) ||
                /* type is a string when converting back */
                (valueType == String.class)) {
            converter = ENUM_CONVERTER;
        } else if (Reflect.isContainerType(valueType)) {
            converter = selectContainerConverter(valueType, info);
        } else {
            converter = ELEMENT_CONVERTER;
        }
        return toObjectConverter(converter);
    }

    private static Converter<?, ?> instantiateConverter(Field field) {
        Convert convert = field.getAnnotation(Convert.class);
        return cache.computeIfAbsent(convert.value(), cls -> {
            Validator.checkConverterHasNoArgsConstructor(cls, field.getName());
            return Reflect.newInstance(cls);
        });
    }

    private static Converter<?, ?> selectContainerConverter(
            Class<?> valueType, Converter.ConversionInfo info
    ) {
        if (info.hasElementType()) {
            return selectElementTypeContainerConverter(valueType);
        } else {
            return selectSimpleContainerConverter(valueType);
        }
    }

    private static Converter<?, ?> selectElementTypeContainerConverter(
            Class<?> valueType
    ) {
        return selector(
                LIST_CONVERTER, SET_CONVERTER, MAP_CONVERTER
        ).apply(valueType);
    }

    private static Converter<?, ?> selectSimpleContainerConverter(
            Class<?> valueType
    ) {
        return selector(
                SIMPLE_LIST_CONVERTER, SIMPLE_SET_CONVERTER, SIMPLE_MAP_CONVERTER
        ).apply(valueType);
    }

    static <R> Function<Class<?>, R> selector(R listValue, R setValue, R mapValue) {
        return containerClass -> {
            if (List.class.isAssignableFrom(containerClass)) {
                return listValue;
            } else if (Set.class.isAssignableFrom(containerClass)) {
                return setValue;
            } else {
                return mapValue;
            }
        };
    }

    static String selectContainerName(Class<?> containerType) {
        return selector("list", "set", "map").apply(containerType);
    }

    private static Converter<Object, Object> toObjectConverter(
            Converter<?, ?> converter
    ) {
        /* This cast may result in a ClassCastException when converting objects
         * back to their original representation. This happens if the type of the
         * converted object has changed for some reason (e.g. by a configuration
         * mistake). However, the ClassCastException is later caught and translated
         * to a ConfigurationException to give additional information about what
         * happened. */
        @SuppressWarnings("unchecked")
        Converter<Object, Object> c = (Converter<Object, Object>) converter;
        return c;
    }

    private static final class SimpleListConverter
            implements Converter<List<?>, List<?>> {
        @Override
        public List<?> convertTo(List<?> element, ConversionInfo info) {
            return element;
        }

        @Override
        public void preConvertTo(ConversionInfo info) {
            Validator.checkContainerValuesNotNull(info);
            Validator.checkContainerValuesSimpleType(info);
        }

        @Override
        public List<?> convertFrom(List<?> element, ConversionInfo info) {
            return element;
        }
    }

    private static final class SimpleSetConverter
            implements Converter<Set<?>, Set<?>> {
        @Override
        public Set<?> convertTo(Set<?> element, ConversionInfo info) {
            return element;
        }

        @Override
        public void preConvertTo(ConversionInfo info) {
            Validator.checkContainerValuesNotNull(info);
            Validator.checkContainerValuesSimpleType(info);
        }

        @Override
        public Set<?> convertFrom(Set<?> element, ConversionInfo info) {
            return element;
        }
    }

    private static final class SimpleMapConverter
            implements Converter<Map<?, ?>, Map<?, ?>> {
        @Override
        public Map<?, ?> convertTo(Map<?, ?> element, ConversionInfo info) {
            return element;
        }

        @Override
        public void preConvertTo(ConversionInfo info) {
            Validator.checkMapKeysAndValues(info);
            Validator.checkContainerValuesSimpleType(info);
        }

        @Override
        public Map<?, ?> convertFrom(Map<?, ?> element, ConversionInfo info) {
            return element;
        }
    }

    private static final class ListConverter
            implements Converter<List<?>, List<?>> {
        @Override
        public List<?> convertTo(List<?> element, ConversionInfo info) {
            if (element.isEmpty()) {
                return element;
            }
            Object o = element.get(0);
            Function<Object, ?> f = createToConversionFunction(o, info);
            return element.stream().map(f).collect(toList());
        }

        @Override
        public void preConvertTo(ConversionInfo info) {
            Validator.checkElementType(info);
            Validator.checkContainerValuesNotNull(info);
            Validator.checkContainerTypes(info);
        }

        @Override
        public List<?> convertFrom(List<?> element, ConversionInfo info) {
            if (element.isEmpty()) {
                return element;
            }
            Object o = element.get(0);
            Function<Object, ?> f = createFromConversionFunction(o, info);
            return element.stream().map(f).collect(toList());
        }

        @Override
        public void preConvertFrom(ConversionInfo info) {
            Validator.checkElementType(info);
        }
    }

    private static final class SetConverter
            implements Converter<Set<?>, Set<?>> {
        @Override
        public Set<?> convertTo(Set<?> element, ConversionInfo info) {
            if (element.isEmpty()) {
                return element;
            }
            Object o = element.iterator().next();
            Function<Object, ?> f = createToConversionFunction(o, info);
            return element.stream().map(f).collect(toSet());
        }

        @Override
        public void preConvertTo(ConversionInfo info) {
            Validator.checkElementType(info);
            Validator.checkContainerValuesNotNull(info);
            Validator.checkContainerTypes(info);
        }

        @Override
        public Set<?> convertFrom(Set<?> element, ConversionInfo info) {
            if (element.isEmpty()) {
                return element;
            }
            Object o = element.iterator().next();
            Function<Object, ?> f = createFromConversionFunction(o, info);
            return element.stream().map(f).collect(toSet());
        }

        @Override
        public void preConvertFrom(ConversionInfo info) {
            Validator.checkElementType(info);
        }
    }

    private static final class MapConverter
            implements Converter<Map<?, ?>, Map<?, ?>> {
        @Override
        public Map<?, ?> convertTo(Map<?, ?> element, ConversionInfo info) {
            if (element.isEmpty()) {
                return element;
            }
            Object o = element.values().iterator().next();
            Function<Object, ?> cf = createToConversionFunction(o, info);
            Function<Map.Entry<?, ?>, ?> f = e -> cf.apply(e.getValue());
            return element.entrySet().stream().collect(toMap(Map.Entry::getKey, f));
        }

        @Override
        public void preConvertTo(ConversionInfo info) {
            Validator.checkElementType(info);
            Validator.checkMapKeysAndValues(info);
            Validator.checkContainerTypes(info);
        }

        @Override
        public Map<?, ?> convertFrom(Map<?, ?> element, ConversionInfo info) {
            if (element.isEmpty()) {
                return element;
            }
            Object o = element.values().iterator().next();
            Function<Object, ?> cf = createFromConversionFunction(o, info);
            Function<Map.Entry<?, ?>, ?> f = e -> cf.apply(e.getValue());
            return element.entrySet().stream().collect(toMap(Map.Entry::getKey, f));
        }

        @Override
        public void preConvertFrom(ConversionInfo info) {
            Validator.checkElementType(info);
        }
    }

    private static Function<Object, ?> createToConversionFunction(
            Object element, Converter.ConversionInfo info
    ) {
        Validator.checkNestingLevel(element, info);
        if (Reflect.isContainerType(element.getClass())) {
            info.incCurrentNestingLevel();
        }
        Converter<Object, ?> converter = selectNonSimpleConverter(
                element.getClass(), info
        );
        return o -> converter.convertTo(o, info);
    }

    private static Function<Object, ?> createFromConversionFunction(
            Object element, Converter.ConversionInfo info
    ) {
        boolean currentLevelSameAsExpected =
                info.getNestingLevel() == info.getCurrentNestingLevel();
        Validator.checkCurrentLevelSameAsExpectedRequiresMapOrString(
                currentLevelSameAsExpected, element, info
        );
        if ((element instanceof Map<?, ?>) && currentLevelSameAsExpected) {
            if (SERIALIZER_FACTORY != null) {
                ObjectSerializer<?, ?> serializer = SERIALIZER_FACTORY.findSerializer(info.getElementType());

                if (serializer != null) {
                    SerializerConverter converter = new SerializerConverter(serializer);
                    return o -> converter.convertFrom(o, info);
                }
            }
            return o -> {
                Map<String, Object> map = toTypeMap(o, null);
                Object inst = Reflect.newInstance(info.getElementType());
                FieldMapper.instanceFromMap(inst, map, info.getMappingInfo());
                return inst;
            };
        } else if ((element instanceof String) && currentLevelSameAsExpected) {
            return createNonSimpleConverter(info.getElementType(), element, info);
        } else {
            info.incCurrentNestingLevel();
            return createNonSimpleConverter(info.getElementType(), element, info);
        }
    }

    private static Function<Object, ?> createNonSimpleConverter(
            Class<?> elementType, Object element, Converter.ConversionInfo info
    ) {
        Converter<?, Object> converter = selectNonSimpleConverter(
                elementType, info
        );
        return o -> converter.convertFrom(o, info);
    }

    private static Map<String, Object> toTypeMap(Object value, String fn) {
        Validator.checkIsMap(value, fn);
        Validator.checkMapKeysAreStrings((Map<?, ?>) value, fn);

        // The following cast won't fail because we just verified that
        // it's a Map<String, Object>.
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;

        return map;
    }

    private static final class IdentityConverter
            implements Converter<Object, Object> {

        @Override
        public Object convertTo(Object element, ConversionInfo info) {
            return element;
        }

        @Override
        public Object convertFrom(Object element, ConversionInfo info) {
            return element;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final class SerializerConverter implements Converter<Object, Object> {

        private ObjectSerializer serializer;

        public SerializerConverter(ObjectSerializer serializer) {
            this.serializer = serializer;
        }

        @Override
        public Object convertTo(Object element, ConversionInfo info) {
            return this.serializer.serialize(element);
        }

        @Override
        public Object convertFrom(Object element, ConversionInfo info) {
            return this.serializer.deserialize(element);
        }
    }

    private static final class SimpleTypeConverter
            implements Converter<Object, Object> {

        @Override
        public Object convertTo(Object element, ConversionInfo info) {
            return element;
        }

        @Override
        public Object convertFrom(Object element, ConversionInfo info) {
            if (info.getFieldType() == element.getClass()) {
                return element;
            }
            if (element instanceof Number) {
                return convertNumber(info.getFieldType(), (Number) element);
            }
            if (element instanceof String) {
                return convertString((String) element);
            }
            return element;
        }

        private Object convertNumber(Class<?> target, Number value) {
            if (target == byte.class || target == Byte.class) {
                return value.byteValue();
            } else if (target == short.class || target == Short.class) {
                return value.shortValue();
            } else if (target == int.class || target == Integer.class) {
                return value.intValue();
            } else if (target == long.class || target == Long.class) {
                return value.longValue();
            } else if (target == float.class || target == Float.class) {
                return value.floatValue();
            } else if (target == double.class || target == Double.class) {
                return value.doubleValue();
            } else {
                String msg = "Number '" + value + "' cannot be converted " +
                        "to type '" + target + "'";
                throw new IllegalArgumentException(msg);
            }
        }

        private Object convertString(String s) {
            int length = s.length();
            if (length == 0) {
                String msg = "An empty string cannot be converted to a character.";
                throw new IllegalArgumentException(msg);
            }
            if (length > 1) {
                String msg = "String '" + s + "' is too long to " +
                        "be converted to a character";
                throw new IllegalArgumentException(msg);
            }
            return s.charAt(0);
        }
    }

    private static final class EnumConverter
            implements Converter<Enum<?>, String> {

        @Override
        public String convertTo(Enum<?> element, ConversionInfo info) {
            return element.toString();
        }

        @Override
        public void preConvertFrom(ConversionInfo info) {
            Validator.checkEnumValueIsString(info);
        }

        @Override
        public Enum<?> convertFrom(String element, ConversionInfo info) {
            Class<? extends Enum> cls = getEnumClass(info);
            try {
                /* cast won't fail because we know that it's an enum */
                @SuppressWarnings("unchecked")
                Enum<?> enm = Enum.valueOf(cls, element);
                return enm;
            } catch (IllegalArgumentException e) {
                Validator.checkElementTypeIsEnumType(cls, info);
                String in = selectWord(info);
                String msg = "Cannot initialize " + in + " because there is no " +
                        "enum constant '" + element + "'.\nValid constants are: " +
                        Arrays.toString(cls.getEnumConstants());
                throw new IllegalArgumentException(msg, e);
            }
        }

        private String selectWord(ConversionInfo info) {
            String fn = info.getFieldName();
            if (Reflect.isContainerType(info.getFieldType())) {
                String w = selectContainerName(info.getValueType());
                return "an enum element of " + w + " '" + fn + "'";
            }
            return "enum '" + fn + "' ";
        }

        @SuppressWarnings("unchecked")
        private Class<? extends Enum> getEnumClass(ConversionInfo info) {
            /* this cast won't fail because this method is only called by a
             * Converter that converts enum types. */
            return (Class<? extends Enum>) (!info.hasElementType()
                    ? info.getValue().getClass()
                    : info.getElementType());
        }
    }

    private static final class ConfigurationElementConverter
            implements Converter<Object, Object> {

        @Override
        public Object convertTo(Object element, ConversionInfo info) {
            return FieldMapper.instanceToMap(element, info.getMappingInfo());
        }

        @Override
        public void preConvertTo(ConversionInfo info) {
            Validator.checkTypeIsConfigurationElement(info.getValueType(), info.getFieldName());
            Validator.checkTypeHasNoArgsConstructor(info);
        }

        @Override
        public Object convertFrom(Object element, ConversionInfo info) {
            Validator.checkElementIsConvertibleToConfigurationElement(element, info);
            Object newInstance = Reflect.newInstance(info.getValueType());
            Map<String, Object> typeMap = toTypeMap(element, info.getFieldName());
            FieldMapper.instanceFromMap(newInstance, typeMap, info.getMappingInfo());
            return newInstance;
        }

        @Override
        public void preConvertFrom(ConversionInfo info) {
            Validator.checkTypeHasNoArgsConstructor(info);
            Validator.checkTypeIsConfigurationElement(info.getValueType(), info.getFieldName());
        }
    }
}