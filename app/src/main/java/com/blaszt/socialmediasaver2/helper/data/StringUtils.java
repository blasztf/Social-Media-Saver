package com.blaszt.socialmediasaver2.helper.data;

public final class StringUtils {
    public static String toUpperCaseFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1, string.length());
    }

    public static String toLowerCaseFirst(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1, string.length());
    }

    public static String toUpperCaseWords(String string) {
        StringBuilder newString = new StringBuilder();
        String[] words = string.split(" ");
        for (String word : words) {
            newString.append(StringUtils.toUpperCaseFirst(word)).append(" ");
        }
        return newString.toString();
    }

    public static String toUpperCaseSentences(String string) {
        StringBuilder newString = new StringBuilder();
        String[] sentences = string.split("\\.");
        for (String sentence : sentences) {
            newString.append(StringUtils.toUpperCaseFirst(sentence)).append(". ");
        }
        return newString.toString();
    }
}
