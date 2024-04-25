package io.fairyproject.util;

import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

@UtilityClass
public class ClassGraphUtil {

    public Stream<Method> methodWithAnnotation(ScanResult scanResult, Class<? extends Annotation> annotationClass) {
        return scanResult.getClassesWithMethodAnnotation(annotationClass).loadClasses()
                .stream()
                .flatMap(clazz -> Stream.of(clazz.getDeclaredMethods()))
                .filter(method -> method.getAnnotation(annotationClass) != null);
    }

    public Stream<Field> fieldWithAnnotation(ScanResult scanResult, Class<? extends Annotation> annotationClass) {
        return scanResult.getClassesWithFieldAnnotation(annotationClass)
                .stream()
                .flatMap(classInfo -> classInfo.getFieldInfo().stream())
                .filter(fieldInfo -> fieldInfo.hasAnnotation(annotationClass))
                .map(FieldInfo::loadClassAndGetField);
    }

}
