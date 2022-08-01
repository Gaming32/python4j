package io.github.gaming32.python4j.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public static int getIntVal(byte[] buf, int offset, int size, ByteOrder order) {
        return getIntVal(buf, offset, size, order, false);
    }

    public static int getIntVal(byte[] buf, int offset, int size, ByteOrder order, boolean signed) {
        final ByteBuffer bb = ByteBuffer.wrap(buf, offset, size).order(order);
        if (size == 4) {
            return bb.getInt();
        }
        if (signed) {
            switch (size) {
                case 1:
                    return bb.get();
                case 2:
                    return bb.getShort();
            }
        } else {
            switch (size) {
                case 1:
                    return bb.get() & 0xff;
                case 2:
                    return bb.getShort() & 0xffff;
            }
        }
        throw new IllegalArgumentException("Unsupported size: " + size);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> orderedMapOfEntries(Map.Entry<K, V>... entries) {
        final Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> orderedMapOf(Object... entries) {
        if ((entries.length & 1) != 0) {
            throw new IllegalArgumentException("Expected even number of entries");
        }
        final Map<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((K)entries[i], (V)entries[i + 1]);
        }
        return Collections.unmodifiableMap(result);
    }
}
