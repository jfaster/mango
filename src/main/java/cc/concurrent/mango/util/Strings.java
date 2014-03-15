package cc.concurrent.mango.util;

/**
 * @author ash
 */
public class Strings {

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0; // string.isEmpty() in Java 6
    }

}
