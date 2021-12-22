package io.fairyproject.gradle;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ExtensionUtil {

    public boolean hasPlatform(Object extension, String target) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method method = extension.getClass().getDeclaredMethod("getFairyPlatforms");
        method.setAccessible(true);

        final Object listProperty = method.invoke(extension);
        final Method get = listProperty.getClass().getMethod("get");

        final List list = (List) get.invoke(listProperty);
        for (Object o : list) {
            final Enum<?> enumObject = Enum.class.cast(o);
            if (enumObject.name().equalsIgnoreCase(target)) {
                return true;
            }
        }

        return false;
    }

}
