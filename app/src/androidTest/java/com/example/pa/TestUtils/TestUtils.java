package com.example.pa.TestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestUtils {
    public static void setFinalField(Object target, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        // 移除 final 修饰符
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(target, newValue);
    }
}