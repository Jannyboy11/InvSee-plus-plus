package com.janboerman.invsee.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;

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

}
