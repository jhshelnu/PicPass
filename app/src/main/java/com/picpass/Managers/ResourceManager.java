package com.picpass.Managers;

import com.picpass.R;

import java.lang.reflect.Field;

public class ResourceManager {

    public static int getDrawableIdFromString(String resName) throws NoSuchFieldException, IllegalAccessException {
        Field field = R.drawable.class.getDeclaredField(resName);
        return field.getInt(field);
    }
}
