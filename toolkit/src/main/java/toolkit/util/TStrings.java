package toolkit.util;

public final class TStrings {

    public static String repeat(String string, int count) {
        return new String(new char[count]).replace("\0", string);
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
