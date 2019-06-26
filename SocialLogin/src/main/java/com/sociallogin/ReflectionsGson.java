package com.sociallogin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionsGson {
    private Object getGsonObject() {
        try {
            Class c = Class.forName("com.google.gson.Gson");
            return c.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object fromJson(String s, Class c) {
        Object o = getGsonObject();
        try {
            if (o != null) {
                Method m = o.getClass().getMethod("fromJson", String.class, Class.class);
                return m.invoke(o, s, c);
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
