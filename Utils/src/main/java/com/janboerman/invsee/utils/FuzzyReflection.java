package com.janboerman.invsee.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class FuzzyReflection {

    private FuzzyReflection() {}

    public static Field[] getFieldOfType(Class<?> owningClass, Class<?> fieldType) {
        Field[] fields = owningClass.getDeclaredFields();
        ArrayList<Field> result = new ArrayList<>(1);
        for (Field field : fields) {
            if (field.getType() == fieldType) {
                field.setAccessible(true);
                result.add(field);
            }
        }
        return result.toArray(Field[]::new);
    }

    public static Method[] getMethodOfType(Class<?> owningClass, Class<?> returnType, Class<?>... parameterTypes) {
        Method[] methods = owningClass.getDeclaredMethods();
        ArrayList<Method> result = new ArrayList<>();
        for (Method method : methods) {
            if (method.getReturnType() == returnType && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                method.setAccessible(true);
                result.add(method);
            }
        }
        return result.toArray(Method[]::new);
    }

}
