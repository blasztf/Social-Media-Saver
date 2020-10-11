package com.blaszt.socialmediasaver2.plugin.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class Parser {
    public static <T extends Parser> T parse(String string, Class<T> clazz) {
        Constructor<T> ctor;
        T obj;

        try {
            ctor = clazz.getDeclaredConstructor(String.class);
            ctor.setAccessible(true);
            obj = ctor.newInstance(string);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            obj = null;
        }

        return obj;
    }

    Parser(String string) {

    }
}
