package com.blaszt.toolkit.util;

public final class TObjects {
    private TObjects() {}

    /**
     * Alias for static method {@linkplain TObjects#nonNull(Object)}.
     * @param obj &nbsp;
     * @return &nbsp;
     */
    public static boolean isNonNull(Object obj) {
        return nonNull(obj);
    }

    public static boolean nonNull(Object obj) {
        return obj != null;
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static String toString(Object obj) {
        return toString(obj, "null");
    }

    public static String toString(Object obj, String nullDefault) {
        if (obj == null) return nullDefault;
        else return obj.toString();
    }

    public static <T> T requireNonNull(T obj) {
        return requireNonNull(obj, null);
    }

    public static <T> T requireNonNull(T obj, String msg) {
        if (obj == null) throw new NullPointerException(msg);
        else return obj;
    }

    public static boolean equals(Object obj1, Object obj2) {
        return obj1 == obj2;
    }

    public static int hashCode(Object obj) {
        return isNull(obj) ? 0 : obj.hashCode();
    }
}
