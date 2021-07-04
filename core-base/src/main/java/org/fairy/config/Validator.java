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

package org.fairy.config;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Validator {

    static void checkNotNull(Object o, String fn) {
        if (o == null) {
            String msg = "The value of field '" + fn + "' is null.\n" +
                    "Please assign a non-null default value or remove this field.";
            throw new ConfigurationException(msg);
        }
    }

    static void checkContainerTypes(Converter.ConversionInfo info) {
        Object value = info.getValue();
        Collection<?> collection = toCollection(value);
        checkCollectionTypes(collection, info);
    }

    private static void checkCollectionTypes(
            Collection<?> collection, Converter.ConversionInfo info
    ) {
        for (Object element : collection) {
            if (Reflect.isContainerType(element.getClass())) {
                Collection<?> container = toCollection(element);
                checkCollectionTypes(container, info);
            } else {
                checkCollectionType(element, info);
            }
        }
    }

    private static void checkCollectionType(Object element, Converter.ConversionInfo info) {
        if (!info.getElementType().isInstance(element)) {
            String cNameField = selectContainerNameField(info);
            String cValues = selectContainerValues(info);
            String msg = "The type of " + cNameField + " doesn't match the " +
                    "type indicated by the ElementType annotation.\n" +
                    "Required type: '" + getClsName(info.getElementType()) +
                    "'\tActual type: '" + getClsName(element.getClass()) +
                    "'\n" + cValues;
            throw new ConfigurationException(msg);
        }
    }

    private static String selectContainerValues(Converter.ConversionInfo info) {
        Object value = info.getValue();
        return Converters.selector(
                "All elements: " + value,
                "All elements: " + value,
                "All entries: " + value
        ).apply(info.getValueType());
    }

    private static String selectContainerNameField(Converter.ConversionInfo info) {
        String fieldName = info.getFieldName();
        return Converters.selector(
                "an element of list '" + fieldName + "'",
                "an element of set '" + fieldName + "'",
                "a value of map '" + fieldName + "'"
        ).apply(info.getValueType());
    }

    private static Collection<?> toCollection(Object container) {
        if (container instanceof List<?> || container instanceof Set<?>) {
            return (Collection<?>) container;
        } else {
            Map<?, ?> map = (Map<?, ?>) container;
            return map.values();
        }
    }

    static void checkMapKeysAndValues(Converter.ConversionInfo info) {
        checkMapKeysSimple((Map<?, ?>) info.getValue(), info.getFieldName());
        checkContainerValuesNotNull(info);
    }

    private static void checkMapKeysSimple(Map<?, ?> map, String fn) {
        for (Object o : map.keySet()) {
            if (!Reflect.isSimpleType(o.getClass())) {
                String msg = "The keys of map '" + fn + "' must be simple types.";
                throw new ConfigurationException(msg);
            }
        }
    }

    static void checkContainerValuesNotNull(Converter.ConversionInfo info) {
        Collection<?> collection = toCollection(info.getValue());
        checkCollectionValuesNotNull(collection, info);
    }

    private static void checkCollectionValuesNotNull(
            Collection<?> col, Converter.ConversionInfo info
    ) {
        for (Object element : col) {
            checkCollectionValueNotNull(element, info);
            if (Reflect.isContainerType(element.getClass())) {
                Collection<?> container = toCollection(element);
                checkCollectionValuesNotNull(container, info);
            }
        }
    }

    private static void checkCollectionValueNotNull(
            Object element, Converter.ConversionInfo info
    ) {
        if (element == null) {
            String cnf = selectContainerNameField(info)
                    .replaceFirst("a", "A");
            String msg = cnf + " is null.\n" +
                    "Please either remove or replace this element." +
                    "\n" + selectContainerValues(info);
            throw new ConfigurationException(msg);
        }
    }

    static void checkContainerValuesSimpleType(Converter.ConversionInfo info) {
        Collection<?> collection = toCollection(info.getValue());
        checkCollectionValuesSimpleType(collection, info);
    }

    private static void checkCollectionValuesSimpleType(
            Collection<?> collection, Converter.ConversionInfo info
    ) {
        for (Object element : collection) {
            if (Reflect.isContainerType(element.getClass())) {
                Collection<?> elements = toCollection(element);
                checkCollectionValuesSimpleType(elements, info);
            } else {
                checkCollectionValueSimpleType(element, info);
            }
        }
    }

    private static void checkCollectionValueSimpleType(
            Object element, Converter.ConversionInfo info
    ) {
        if (!Reflect.isSimpleType(element.getClass())) {
            String cn = Converters.selectContainerName(info.getValueType());
            String cnf = selectContainerNameField(info);
            String fieldName = info.getFieldName();
            String msg = "The type of " + cnf + " is not a simple type but " + cn +
                    " '" + fieldName + "' is missing the ElementType annotation." +
                    "\n" + selectContainerValues(info);
            throw new ConfigurationException(msg);
        }
    }

    static void checkTypeIsConfigurationElement(Class<?> cls, String fn) {
        if (!Reflect.isConfigurationElement(cls)) {
            String msg = "Type '" + getClsName(cls) + "' of field '" +
                    fn + "' is not annotated as a configuration element.";
            throw new ConfigurationException(msg);
        }
    }

    private static String getClsName(Class<?> cls) {
        String clsName = cls.getSimpleName();
        if (clsName.equals("")) {
            clsName = cls.getName();
        }
        return clsName;
    }

    static void checkIsMap(Object value, String fn) {
        Class<?> cls = value.getClass();
        if (!Map.class.isAssignableFrom(cls)) {
            String msg = "Initializing field '" + fn + "' requires a " +
                    "Map<String, Object> but the given object is not a map.\n" +
                    "Type: '" + cls.getSimpleName() + "'\tValue: '" + value + "'";
            throw new ConfigurationException(msg);
        }
    }

    static void checkMapKeysAreStrings(Map<?, ?> map, String fn) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if ((key == null) || (key.getClass() != String.class)) {
                String msg = "Initializing field '" + fn + "' requires a " +
                        "Map<String, Object> but the given map contains " +
                        "non-string keys.\nAll entries: " + map;
                throw new ConfigurationException(msg);
            }
        }
    }

    static void checkElementType(Converter.ConversionInfo info) {
        Class<?> elementType = info.getElementType();
        if (!elementType.isEnum())
            checkElementTypeIsConfigurationElement(info);
        checkElementTypeIsConcrete(info);
        if (!elementType.isEnum())
            checkElementTypeHasNoArgsConstructor(info);
    }

    static void checkFieldWithElementTypeIsContainer(Converter.ConversionInfo info) {
        boolean isContainer = Reflect.isContainerType(info.getValueType());
        if (info.hasElementType() && !isContainer) {
            String msg = "Field '" + info.getFieldName() + "' is annotated with " +
                    "the ElementType annotation but is not a List, Set or Map.";
            throw new ConfigurationException(msg);
        }
    }

    private static void checkElementTypeIsConfigurationElement(Converter.ConversionInfo info) {
        Class<?> elementType = info.getElementType();
        if (!Reflect.isConfigurationElement(elementType)) {
            String msg = "The element type '" + getClsName(elementType) + "'" +
                    " of field '" + info.getFieldName() + "' is not a " +
                    "configuration element.";
            throw new ConfigurationException(msg);
        }
    }

    private static void checkElementTypeIsConcrete(Converter.ConversionInfo info) {
        Class<?> elementType = info.getElementType();

        String msg = getType(elementType);
        if (msg != null) {
            msg = "The element type of field '" + info.getFieldName() + "' must " +
                    "be a concrete class but type '" +
                    getClsName(elementType) + "' is " + msg;
            throw new ConfigurationException(msg);
        }
    }

    private static String getType(Class<?> cls) {
        String msg = null;

        if (cls.isInterface()) {
            msg = "an interface.";
        } else if (cls.isPrimitive()) {
            msg = "primitive.";
        } else if (cls.isArray()) {
            msg = "an array.";
        } else if (Modifier.isAbstract(cls.getModifiers())) {
            msg = "an abstract class.";
        }
        return msg;
    }

    private static void checkElementTypeHasNoArgsConstructor(Converter.ConversionInfo info) {
        Class<?> elementType = info.getElementType();
        if (!Reflect.hasNoArgConstructor(elementType)) {
            String msg = "The element type '" + elementType.getSimpleName() + "'" +
                    " of field '" + info.getFieldName() + "' doesn't have " +
                    "a no-args constructor.";
            throw new ConfigurationException(msg);
        }
    }

    static void checkTypeHasNoArgsConstructor(Converter.ConversionInfo info) {
        Class<?> valueType = info.getValueType();
        if (!Reflect.hasNoArgConstructor(valueType)) {
            String msg = "Type '" + getClsName(valueType) + "' of field '" +
                    info.getFieldName() + "' doesn't have a no-args constructor.";
            throw new ConfigurationException(msg);
        }
    }

    static void checkConverterHasNoArgsConstructor(Class<?> converterClass, String fn) {
        if (!Reflect.hasNoArgConstructor(converterClass)) {
            String msg = "Converter '" + converterClass.getSimpleName() + "' used " +
                    "on field '" + fn + "' doesn't have a no-args constructor.";
            throw new ConfigurationException(msg);
        }
    }

    static void checkEnumValueIsString(Converter.ConversionInfo info) {
        Object val = info.getMapValue();
        if (!(val instanceof String)) {
            String sn = val.getClass().getSimpleName();
            String msg = "Initializing enum '" + info.getFieldName() + "' " +
                    "requires a string but '" + val + "' is of type '" + sn + "'.";
            throw new ConfigurationException(msg);
        }
    }

    static void checkFieldTypeAssignableFrom(Class<?> type, Converter.ConversionInfo info) {
        Class<?> fieldType = info.getFieldType();
        if (!fieldType.isAssignableFrom(type)) {
            String msg = "Can not set field '" + info.getFieldName() + "' with " +
                    "type '" + getClsName(fieldType) + "' to '" +
                    getClsName(type) + "'.";
            throw new ConfigurationException(msg);
        }
    }

    static void checkElementIsConvertibleToConfigurationElement(
            Object element, Converter.ConversionInfo info
    ) {
        Class<?> eClass = element.getClass();
        if (Reflect.isContainerType(info.getFieldType()) &&
                !Map.class.isAssignableFrom(eClass)) {
            String msg = "Initializing field '" + info.getFieldName() + "' " +
                    "requires objects of type Map<String, Object> but element " +
                    "'" + element + "' is of type '" + getClsName(eClass) + "'.";
            throw new IllegalArgumentException(msg);
        }
    }

    static void checkNestingLevel(Object element, Converter.ConversionInfo info) {
        if (!Reflect.isContainerType(element.getClass())) {
            if (info.getNestingLevel() != info.getCurrentNestingLevel()) {
                String msg = "Field '" + info.getFieldName() + "' of class " +
                        "'" + getClsName(info.getInstance().getClass()) + "' " +
                        "has a nesting level of " + info.getNestingLevel() +
                        " but the first object of type '" +
                        getClsName(info.getElementType()) + "' was found on " +
                        "level " + info.getCurrentNestingLevel() + ".";
                throw new ConfigurationException(msg);
            }
        }
    }

    static void checkCurrentLevelSameAsExpectedRequiresMapOrString(
            boolean currentLevelSameAsExpected,
            Object element, Converter.ConversionInfo info
    ) {
        boolean isMapOrString = (element instanceof Map<?, ?>) ||
                (element instanceof String);
        if (currentLevelSameAsExpected && !isMapOrString) {
            Class<?> cls = info.getInstance().getClass();
            String msg = "Field '" + info.getFieldName() + "' of class '" +
                    getClsName(cls) + "' has a nesting level" +
                    " of " + info.getNestingLevel() + " but element '" + element +
                    "' of type '" + getClsName(element.getClass()) + "' cannot be " +
                    "converted to '" + getClsName(info.getElementType()) + "'.";
            throw new ConfigurationException(msg);
        }
    }

    static void checkElementTypeIsEnumType(Class<?> type, Converter.ConversionInfo info) {
        if (!Reflect.isEnumType(type)) {
            String msg = "Element type '" + getClsName(type) + "' of field " +
                    "'" + info.getFieldName() + "' is not an enum type.";
            throw new IllegalArgumentException(msg);
        }
    }

    static void checkConverterNotReturnsNull(Object converted, Converter.ConversionInfo info) {
        if (converted == null) {
            String msg = "Failed to convert value '" + info.getValue() + "' of " +
                    "field '" + info.getFieldName() + "' because the converter " +
                    "returned null.";
            throw new ConfigurationException(msg);
        }
    }
}
