package io.github.gaming32.python4j;

import java.util.OptionalInt;

public final class Utils {
    private Utils() {
    }

    public static OptionalInt boxedToOptional(Integer i) {
        return i == null ? OptionalInt.empty() : OptionalInt.of(i.intValue());
    }

    public static String leftJustify(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }

    public static String rightJustify(String s, int width) {
        if (s.length() >= width) return s;
        return " ".repeat(width - s.length()) + s;
    }
}
