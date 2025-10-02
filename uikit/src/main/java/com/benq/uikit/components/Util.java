package com.benq.uikit.components;

import android.util.Log;

import java.lang.reflect.Method;

public class Util {
    public static String getSystemProperty(String key, String defaultValue) throws IllegalArgumentException {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");

            @SuppressWarnings("rawtypes")
            Class[] paramTypes = {String.class, String.class};
            Method get = SystemProperties.getMethod("get", paramTypes);

            //Parameters
            Object[] params = {key, defaultValue};
            return (String) get.invoke(SystemProperties, params);
        } catch (Exception e) {
            Log.e("Util getSystemProperty", "no system property available : " + key);
            return defaultValue;
        }
    }


}
