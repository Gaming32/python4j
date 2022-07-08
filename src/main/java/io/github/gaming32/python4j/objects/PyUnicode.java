package io.github.gaming32.python4j.objects;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class PyUnicode extends PyObject {
    private static final class GlobalStrings {
        static PyUnicode empty = new PyUnicode(new byte[0], KIND_1BYTE | FLAG_COMPACT | FLAG_ASCII | FLAG_INTERNED);
        static PyUnicode[] ascii = new PyUnicode[128];
        static PyUnicode[] latin1 = new PyUnicode[128];

        static {
            for (int i = 0; i < 128; i++) {
                ascii[i] = new PyUnicode(new byte[] { (byte)i }, KIND_1BYTE | FLAG_COMPACT | FLAG_ASCII | FLAG_INTERNED);
                latin1[i] = new PyUnicode(new byte[] { (byte)(i + 128) }, KIND_1BYTE | FLAG_COMPACT | FLAG_INTERNED);
            }
        }

        static PyUnicode latin1(int ch) {
            return ch < 128 ? ascii[ch] : latin1[ch - 128];
        }
    }

    private static final int MAX_UNICODE = 0x10ffff;

    public static final int KIND_1BYTE = 0x0;
    public static final int KIND_2BYTE = 0x1;
    public static final int KIND_4BYTE = 0x2;
    private static final int KIND_MASK = KIND_1BYTE | KIND_2BYTE | KIND_4BYTE;

    private static final int FLAG_COMPACT = 0x4;
    private static final int FLAG_ASCII = 0x8;
    private static final int FLAG_INTERNED = 0x10;
    private static final int FLAG_SHIFT = 3;

    private long hash = -1L;
    private byte kindAndFlags;
    private final byte[] data;

    private PyUnicode(byte[] data, int kindAndFlags) {
        this.data = data;
        this.kindAndFlags = (byte)kindAndFlags;
    }

    private PyUnicode(int size) {
        data = new byte[size];
    }

    @Override
    public String __str__() {
        switch (getKind()) {
            case KIND_1BYTE:
                return new String(data, 0, data.length, StandardCharsets.ISO_8859_1);
            case KIND_2BYTE:
                return new String(data, 0, data.length, StandardCharsets.UTF_16BE);
            case KIND_4BYTE:
                try {
                    return new String(data, 0, data.length, "UTF-32BE");
                } catch (UnsupportedEncodingException e) {
                    throw new Error(e);
                }
            default:
                throw new AssertionError();
        }
    }

    @Override
    public String __repr__() {
        boolean hasSingleQuote = false;
        for (int i = 0; i < length(); i++) {
            int c = getCodePoint(i);
            if (c == '\'') {
                hasSingleQuote = true;
                break;
            }
        }
        char quote, escapeQuote;
        if (hasSingleQuote) {
            quote = '"';
            escapeQuote = '\'';
        } else {
            quote = '\'';
            escapeQuote = '"';
        }
        StringBuilder sb = new StringBuilder().append(quote);
        for (int i = 0; i < length(); i++) {
            int c = getCodePoint(i);
            if (c == escapeQuote) {
                sb.append('\\').append(escapeQuote);
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c == '\f') {
                sb.append("\\f");
            } else if (c == '\b') {
                sb.append("\\b");
            } else if (c >= 32 && c < 128) {
                sb.append((char)c);
            } else if (c < 0x100) {
                sb.append("\\x").append(String.format("%02x", c));
            } else if (c < 0x10000) {
                sb.append("\\u").append(String.format("%04x", c));
            } else {
                sb.append("\\U").append(String.format("%08x", c));
            }
        }
        return sb.append(quote).toString();
    }

    public int length() {
        return data.length >> getKind();
    }

    @Override
    public long __hash__() {
        if (hash != -1L) {
            return hash;
        }
        return hash = PyBytes.hash(data);
    }

    public int getCodePoint(int index) {
        int kind = getKind();
        index <<= kind;
        if (kind == KIND_1BYTE) {
            return data[index] & 0xff;
        } else if (kind == KIND_2BYTE) {
            return ((data[index] & 0xff) << 8) | (data[index + 1] & 0xff);
        } else if (kind == KIND_4BYTE) {
            return ((data[index] & 0xff) << 24) | ((data[index + 1] & 0xff) << 16) | ((data[index + 2] & 0xff) << 8) | (data[index + 3] & 0xff);
        } else {
            throw new AssertionError();
        }
    }

    public int getKind() {
        return kindAndFlags & KIND_MASK;
    }

    public int getFlags() {
        return kindAndFlags;
    }

    public static PyUnicode fromSizeAndMax(int size, int maxChar) {
        if (size == 0) {
            return GlobalStrings.empty;
        }

        int kind;
        int charSize;
        boolean isAscii = false;
        if (maxChar < 128) {
            kind = KIND_1BYTE;
            charSize = 1;
            isAscii = true;
        } else if (maxChar < 256) {
            kind = KIND_1BYTE;
            charSize = 1;
        } else if (maxChar < 65536) {
            kind = KIND_2BYTE;
            charSize = 2;
        } else {
            if (maxChar > MAX_UNICODE) {
                throw new IllegalArgumentException("invalid maximum character passed to PyUnicode.fromSizeAndMax");
            }
            kind = KIND_4BYTE;
            charSize = 4;
        }

        if (size < 0) {
            throw new IllegalArgumentException("Negative size passed to PyUnicode.fromSizeAndMax");
        }

        PyUnicode result = new PyUnicode(size * charSize);
        result.kindAndFlags = (byte)(kind | FLAG_COMPACT | (isAscii ? FLAG_ASCII : 0));
        return result;
    }

    public static PyUnicode fromKindAndData(int kind, byte[] data, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        switch (kind) {
            case KIND_1BYTE:
                return fromUCS1(data, size);
            default:
                throw new IllegalAccessError("invalid kind");
        }
    }

    public static PyUnicode decodeUTF8(byte[] data, int size, String errors) {
        // TODO: implement
        throw new AssertionError("Unicode not implemented yet.");
    }

    public static PyUnicode fromString(String s) {
        boolean utf16 = false;
        boolean isAscii = true;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c > 127) {
                isAscii = false;
                if (c > 255) {
                    utf16 = true;
                    break;
                }
            }
        }
        return new PyUnicode(
            s.getBytes(utf16 ? StandardCharsets.UTF_16BE : StandardCharsets.ISO_8859_1),
            conversionFlags(utf16, isAscii)
        );
    }

    public static PyUnicode fromCharArray(char[] s) {
        boolean utf16 = false;
        boolean isAscii = true;
        for (int i = 0; i < s.length; i++) {
            if (s[i] > 127) {
                isAscii = false;
                if (s[i] > 255) {
                    utf16 = true;
                    break;
                }
            }
        }
        final byte[] result = new byte[utf16 ? s.length << 1 : s.length];
        if (utf16) {
            for (int i = 0; i < s.length; i++) {
                result[i << 1] = (byte)(s[i] >> 8);
                result[(i << 1) + 1] = (byte)s[i];
            }
        } else {
            for (int i = 0; i < s.length; i++) {
                result[i] = (byte)s[i];
            }
        }
        return new PyUnicode(result, conversionFlags(utf16, isAscii));
    }

    @Override
    public boolean __bool__() {
        return data.length > 0;
    }

    private static int conversionFlags(boolean utf16, boolean isAscii) {
        return FLAG_COMPACT | (utf16 ? KIND_2BYTE : KIND_1BYTE) | (isAscii ? FLAG_ASCII : 0);
    }

    private static PyUnicode fromUCS1(byte[] u, int size) {
        if (size == 0) {
            return GlobalStrings.empty;
        }
        assert size > 0;
        if (size == 1) {
            return GlobalStrings.latin1(u[0] & 0xff);
        }

        int maxChar = ucs1FindMaxChar(u, 0, size);
        PyUnicode result = fromSizeAndMax(size, maxChar);
        System.arraycopy(u, 0, result.data, 0, size);
        return result;
    }

    private static int ucs1FindMaxChar(byte[] data, int begin, int end) {
        int p = begin;

        while (p < end) {
            if ((data[p++] & 0x80) != 0) {
                return 255;
            }
        }
        return 127;
    }
}
