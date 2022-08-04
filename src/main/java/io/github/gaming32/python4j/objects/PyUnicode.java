package io.github.gaming32.python4j.objects;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.github.gaming32.python4j.CharType;
import io.github.gaming32.python4j.UnicodeType;

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

    /** StringBuilder equivalent for PyUnicode */
    public static final class Builder {
        byte[] data;
        int length;
        byte kind;
        boolean isAscii;

        public Builder() {
            data = new byte[16];
            length = 0;
            kind = KIND_1BYTE;
            isAscii = true;
        }

        public Builder(int capacity) {
            data = new byte[capacity];
            length = 0;
            kind = KIND_1BYTE;
            isAscii = true;
        }

        public Builder(PyUnicode other) {
            data = Arrays.copyOf(other.data, other.data.length + 16);
            length = other.data.length;
            kind = other.getKind();
            isAscii = other.isAscii();
        }

        private void ensureCapacity(int capacity) {
            if (length + capacity > data.length) {
                data = Arrays.copyOf(data, getNewSize(data.length, capacity));
            }
        }

        private static int getNewSize(int size, int minimum) {
            return Math.max(size * 2, size + minimum);
        }

        public int length() {
            return length >> kind;
        }

        public Builder append(PyUnicode other) {
            final byte otherKind = other.getKind();
            if (otherKind > kind) {
                byte[] newData = new byte[getNewSize(data.length << (otherKind - kind), other.data.length)];
                inflate(data, newData, 0, length, kind, otherKind);
                System.arraycopy(other.data, 0, newData, length << (otherKind - kind), other.data.length);
                data = newData;
                length = (length << (otherKind - kind)) + other.data.length;
                kind = otherKind;
                isAscii &= other.isAscii();
            } else if (otherKind == kind) {
                ensureCapacity(other.data.length);
                System.arraycopy(other.data, 0, data, length, other.data.length);
                length += other.data.length;
                isAscii = false;
            } else /* otherKind < kind */ {
                ensureCapacity(other.data.length << (kind - otherKind));
                inflate(other.data, data, length, other.data.length, otherKind, kind);
                length += other.data.length << (kind - otherKind);
                isAscii = false;
            }
            return this;
        }

        public Builder append(int ch) {
            if (ch < 255) {
                ensureCapacity(1 << kind);
                putKind(data, length >> kind, ch, kind);
                if (ch > 127) {
                    isAscii = false;
                }
                length += 1 << kind;
            } else if (ch < 65536) {
                byte[] newData;
                if (kind < KIND_2BYTE) {
                    newData = new byte[getNewSize(data.length, 1) << KIND_2BYTE];
                    inflate(data, newData, 0, length, kind, KIND_2BYTE);
                    length <<= 1;
                    kind = KIND_2BYTE;
                } else {
                    ensureCapacity(1 << kind);
                    newData = data;
                }
                putKind(newData, length >> kind, ch, kind);
                length += 1 << kind;
                data = newData;
                isAscii = false;
            } else {
                if (ch > MAX_UNICODE) {
                    throw new IllegalArgumentException("Unicode character out of range: " + ch);
                }
                byte[] newData;
                if (kind < KIND_4BYTE) {
                    newData = new byte[getNewSize(data.length, 1) << (KIND_4BYTE - kind)];
                    inflate(data, newData, 0, length, kind, KIND_4BYTE);
                    length <<= KIND_4BYTE - kind;
                    kind = KIND_4BYTE;
                } else {
                    ensureCapacity(1 << kind);
                    newData = data;
                }
                putKind(newData, length >> kind, ch, kind);
                length += 1 << kind;
                data = newData;
                isAscii = false;
            }
            return this;
        }

        public Builder appendInt(int i) {
            return append(PyLong.fromInt(i).__repr__());
        }

        public Builder appendJavaString(String s) {
            byte otherKind = KIND_1BYTE;
            boolean isAscii = true;
            for (int i = 0; i < s.length(); i++) {
                int cp = s.codePointAt(i);
                if (cp > 127) {
                    isAscii = false;
                    if (cp > 255) {
                        otherKind = KIND_2BYTE;
                        if (cp > 65535) {
                            otherKind = KIND_4BYTE;
                            break;
                        }
                    }
                }
            }
            final int n = otherKind == KIND_4BYTE ? s.codePointCount(0, s.length()) : s.length();
            int outPos = length;
            if (otherKind > kind) {
                byte[] newData = new byte[getNewSize(data.length, n << otherKind)];
                inflate(data, newData, 0, length, kind, otherKind);
                data = newData;
                kind = otherKind;
                length = (length << (otherKind - kind)) + (n << otherKind);
                this.isAscii &= isAscii;
            } else if (otherKind == kind) {
                ensureCapacity(n << otherKind);
                length += n << otherKind;
            } else /* otherKind < kind */ {
                ensureCapacity(n << kind);
                length += n << kind;
            }
            outPos >>= kind;
            if (otherKind < KIND_4BYTE) {
                for (int i = 0; i < s.length(); i++) {
                    putKind(data, outPos++, s.charAt(i), kind);
                }
            } else {
                for (int i = 0; i < s.length();) {
                    final int cp = s.codePointAt(i);
                    putKind(data, outPos++, cp, kind);
                    i += Character.charCount(cp);
                }
            }
            return this;
        }

        public PyUnicode finish() {
            return new PyUnicode(Arrays.copyOf(data, length), (byte)(kind | FLAG_COMPACT | (isAscii ? FLAG_ASCII : 0)));
        }
    }

    public static final Charset UTF_32BE = Charset.forName("UTF-32BE");

    private static final int MAX_UNICODE = 0x10ffff;

    public static final byte KIND_1BYTE = 0x0;
    public static final byte KIND_2BYTE = 0x1;
    public static final byte KIND_4BYTE = 0x2;
    private static final byte KIND_MASK = KIND_1BYTE | KIND_2BYTE | KIND_4BYTE;

    private static final byte FLAG_COMPACT = 0x4;
    private static final byte FLAG_ASCII = 0x8;
    private static final byte FLAG_INTERNED = 0x10;
    private static final byte FLAG_SHIFT = 3;

    private long hash = -1L;
    private byte kindAndFlags;
    private byte[] data;

    private PyUnicode(byte[] data, int kindAndFlags) {
        this.data = data;
        this.kindAndFlags = (byte)kindAndFlags;
    }

    private PyUnicode(int size) {
        data = new byte[size];
    }

    public static PyUnicode empty() {
        return GlobalStrings.empty;
    }

    @Override
    public String toString() {
        switch (getKind()) {
            case KIND_1BYTE:
                return new String(data, 0, data.length, StandardCharsets.ISO_8859_1);
            case KIND_2BYTE:
                return new String(data, 0, data.length, StandardCharsets.UTF_16BE);
            case KIND_4BYTE:
                return new String(data, 0, data.length, UTF_32BE);
            default:
                throw new AssertionError();
        }
    }

    @Override
    public PyUnicode __str__() {
        return this;
    }

    @Override
    public PyUnicode __repr__() {
        boolean hasSingleQuote = false;
        for (int i = 0; i < length(); i++) {
            int c = getCodePoint(i);
            if (c == '\'') {
                hasSingleQuote = true;
                break;
            }
        }
        char quote;
        if (hasSingleQuote) {
            quote = '"';
        } else {
            quote = '\'';
        }
        final Builder result = new Builder().append(quote);
        for (int i = 0; i < length(); i++) {
            int c = getCodePoint(i);
            if (c >= ' ' && c < 0x7F) {
                result.append((char)c);
            } else if (c == quote) {
                result.append('\\').append(quote);
            } else if (c == '\n') {
                result.append('\\').append('n');
            } else if (c == '\r') {
                result.append('\\').append('r');
            } else if (c == '\t') {
                result.append('\\').append('t');
            } else if (c == '\f') {
                result.append('\\').append('f');
            } else if (c == '\b') {
                result.append('\\').append('b');
            } else if (c < ' ' || c == 0x7F) {
                result.append('\\').append('x');
                if (c < 0x10) {
                    result.append('0').append(Character.forDigit(c & 0xf, 16));
                } else {
                    result.append(Character.forDigit(c >> 4, 16))
                        .append(Character.forDigit(c & 0xf, 16));
                }
            } else if (UnicodeType.isPrintable(c)) {
                result.append(c);
            } else if (c < 0xff) {
                result.append('\\').append('x');
                if (c < 0x10) {
                    result.append('0').append(Character.forDigit(c & 0xf, 16));
                } else {
                    result.append(Character.forDigit(c >> 4, 16))
                        .append(Character.forDigit(c & 0xf, 16));
                }
            } else if (c < 0x10000) {
                result.appendJavaString(String.format("\\u%04x", c));
            } else {
                result.appendJavaString(String.format("\\U%08x", c));
            }
        }
        return result.append(quote).finish();
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
        final int kind = getKind();
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

    public PyUnicode concat(PyUnicode other) {
        final int kind = getKind(), otherKind = other.getKind();
        final int resultKind = Math.max(kind, otherKind);
        final byte[] result = new byte[(length() + other.length()) << resultKind];
        if (kind == otherKind) {
            System.arraycopy(data, 0, result, 0, data.length);
            System.arraycopy(other.data, 0, result, data.length, other.data.length);
        } else if (kind < otherKind) {
            inflate(data, result, 0, data.length, kind, otherKind);
            System.arraycopy(other.data, 0, result, data.length << (otherKind - kind), other.data.length);
        } else {
            System.arraycopy(data, 0, result, 0, data.length);
            inflate(other.data, result, data.length, other.data.length, otherKind, kind);
        }
        return new PyUnicode(
            result,
            resultKind | FLAG_COMPACT | ((kindAndFlags & FLAG_ASCII) & (other.kindAndFlags & FLAG_ASCII))
        );
    }

    public PyUnicode concatMultiple(PyUnicode... others) {
        int resultKind = getKind(), kind = resultKind;
        int resultLength = length();
        int isAscii = kindAndFlags & FLAG_ASCII;
        for (PyUnicode other : others) {
            resultKind = Math.max(resultKind, other.getKind());
            resultLength += other.length();
            isAscii &= other.kindAndFlags & FLAG_ASCII;
        }
        final byte[] result = new byte[resultLength << resultKind];
        int dest;
        if (kind == resultKind) {
            System.arraycopy(data, 0, result, 0, dest = data.length);
        } else {
            inflate(data, result, 0, data.length, kind, resultKind);
            dest = data.length << (resultKind - kind);
        }
        for (PyUnicode other : others) {
            if (other.getKind() == resultKind) {
                System.arraycopy(other.data, 0, result, dest, other.data.length);
                dest += other.data.length;
            } else {
                inflate(other.data, result, dest, other.data.length, other.getKind(), resultKind);
                dest += other.data.length << (resultKind - other.getKind());
            }
        }
        return new PyUnicode(result, resultKind | isAscii | FLAG_COMPACT);
    }

    private static void inflate(byte[] in, byte[] out, int dest, int len, int fromKind, int toKind) {
        final int inBytes = 1 << fromKind;
        final int padBytes = (1 << toKind) - inBytes;
        for (int i = 0; i < len; i += inBytes, dest += inBytes) {
            for (int j = 0; j < padBytes; j++) {
                out[dest++] = 0;
            }
            System.arraycopy(in, i, out, dest, inBytes);
        }
    }

    private static void deflate(byte[] in, byte[] out, int dest, int len, int fromKind, int toKind) {
        final int diff = 1 << (fromKind - toKind);
        final int outBytes = 1 << toKind;
        for (int i = diff - 1; i < len; i += diff, dest++) {
            System.arraycopy(in, i, out, dest, outBytes);
        }
    }

    @SuppressWarnings("unused")
    private static void convert(byte[] in, byte[] out, int dest, int len, int fromKind, int toKind) {
        if (toKind == fromKind) return;
        if (toKind < fromKind) {
            deflate(in, out, dest, len, fromKind, toKind);
        } else {
            inflate(in, out, dest, len, fromKind, toKind);
        }
    }

    @Override
    public PyObject __add__(PyObject other) {
        if (other instanceof PyUnicode) {
            return concat((PyUnicode)other);
        }
        return PyNotImplemented.NotImplemented;
    }

    public PyUnicode repeat(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be less than 0");
        }
        if (count == 0) {
            return GlobalStrings.empty;
        }
        if (count == 1) {
            return this;
        }
        final byte[] result = new byte[data.length * count];
        for (int i = 0, p = 0; i < count; i++) {
            System.arraycopy(data, 0, result, p, data.length);
            p += data.length;
        }
        return new PyUnicode(result, kindAndFlags & ~FLAG_INTERNED);
    }

    @Override
    public PyObject __mul__(PyObject other) {
        if (other instanceof PyLong) {
            final int[] longAndOverflow = ((PyLong)other).asLongAndOverflow();
            if (longAndOverflow[1] != 0) {
                throw new IllegalArgumentException("PyLong too large");
            }
            return repeat(longAndOverflow[0]);
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public PyObject __rmul__(PyObject other) {
        return __mul__(other);
    }

    public byte getKind() {
        return (byte)(kindAndFlags & KIND_MASK);
    }

    public byte getFlags() {
        return (byte)kindAndFlags;
    }

    public boolean isAscii() {
        return (kindAndFlags & FLAG_ASCII) != 0;
    }

    public int getMaxCharValue() {
        if (isAscii()) {
            return 0x7f;
        }

        int kind = getKind();
        if (kind == KIND_1BYTE) {
            return 0xff;
        }
        if (kind == KIND_2BYTE) {
            return 0xffff;
        }
        assert kind == KIND_4BYTE;
        return 0x10ffff;
    }

    public byte[] getLatin1() {
        if (getKind() != KIND_1BYTE) {
            throw new IllegalArgumentException("Can only call getLatin1() on latin-1 compatible PyUnicode");
        }
        return data.clone();
    }

    public byte[] asEncodedString(String encoding, String errors) {
        if (encoding == null) {
            return asUtf8String(errors, ERROR_UNKNOWN);
        }

        final String normalized = normalizeEncoding(encoding);
        if (normalized.startsWith("utf")) {
            final int i = normalized.charAt(3) == '_' ? 4 : 3;
            if (normalized.length() == i + 1 && normalized.charAt(i) == '8') {
                return asUtf8String(errors, ERROR_UNKNOWN);
            }
        }

        throw new IllegalArgumentException("Unsupported encoding: " + encoding);
    }

    private byte[] asUtf8String(String errors, byte errorHandler) {
        if (isAscii()) {
            return data.clone();
        }

        final byte kind = getKind();
        final int size = length();
        final byte[] result = new byte[data.length * (kind + 2)];
        int p = 0;

        for (int i = 0; i < size;) {
            int ch = getKind(data, i++, kind);

            if (ch < 0x80) {
                result[p++] = (byte)ch;
            } else if (ch < 0x0800) {
                result[p++] = (byte)(0xc0 | (ch >> 6));
                result[p++] = (byte)(0x80 | (ch & 0x3f));
            } else if (isSurrogate(ch)) {
                if (errorHandler == ERROR_UNKNOWN) {
                    errorHandler = getErrorHandler(errors);
                }

                int startPos = i - 1;
                int endPos = startPos + 1;

                while (endPos < size && isSurrogate(getKind(data, endPos, kind))) {
                    endPos++;
                }

                switch (errorHandler) {
                    case ERROR_REPLACE:
                        Arrays.fill(result, p, p += endPos - startPos, (byte)'?');
                    case ERROR_IGNORE:
                        i += endPos - startPos - 1;
                        break;

                    case ERROR_SURROGATEPASS:
                        for (int k = startPos; k < endPos; k++) {
                            ch = getKind(data, k, kind);
                            result[p++] = (byte)(0xe0 | (ch >> 12));
                            result[p++] = (byte)(0x80 | ((ch >> 6) & 0x3f));
                            result[p++] = (byte)(0x80 | (ch & 0x3f));
                        }
                        i += endPos - startPos - 1;

                    case ERROR_SURROGATEESCAPE: {
                        int k;
                        for (k = startPos; k < endPos; k++) {
                            ch = getKind(data, k, kind);
                            if (!(0xDC80 <= ch && ch <= 0xDCFF)) {
                                break;
                            }
                            result[p++] = (byte)(ch & 0xff);
                        }
                        if (k >= endPos) {
                            i += endPos - startPos - 1;
                            break;
                        }
                        startPos = k;
                        assert startPos < endPos;
                    }
                    default:
                        throw new UnsupportedOperationException("Unsupported errors: " + errors);
                }
            } else if (ch < 0x10000) {
                result[p++] = (byte)(0xe0 | (ch >> 12));
                result[p++] = (byte)(0x80 | ((ch >> 6) & 0x3f));
                result[p++] = (byte)(0x80 | (ch & 0x3f));
            } else {
                assert ch <= MAX_UNICODE;
                result[p++] = (byte)(0xf0 | (ch >> 18));
                result[p++] = (byte)(0x80 | ((ch >> 12) & 0x3f));
                result[p++] = (byte)(0x80 | ((ch >> 6) & 0x3f));
                result[p++] = (byte)(0x80 | (ch & 0x3f));
            }
        }

        return p == result.length ? result : Arrays.copyOf(result, p);
    }

    private static String normalizeEncoding(String encoding) {
        final StringBuilder result = new StringBuilder(encoding.length());
        boolean punct = false;
        for (int i = 0; i < encoding.length(); i++) {
            final char c = encoding.charAt(i);
            if (CharType.isAlnum(c) || c == '.') {
                if (punct && result.length() > 0) {
                    result.append('_');
                }
                punct = false;

                result.append((char)CharType.toLower(c));
            } else {
                punct = true;
            }
        }
        return result.toString();
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
            case KIND_2BYTE:
                return fromUCS2(data, size);
            case KIND_4BYTE:
                return fromUCS4(data, size);
            default:
                throw new IllegalArgumentException("invalid kind");
        }
    }

    public static PyUnicode decodeUTF8(byte[] data, String errors) {
        return decodeUTF8Stateful(data, data.length, errors, null);
    }

    public static PyUnicode decodeUTF8(byte[] data, int size, String errors) {
        return decodeUTF8Stateful(data, size, errors, null);
    }

    public static PyUnicode decodeUTF8Stateful(byte[] data, int size, String errors, int[] consumed) {
        return unicodeDecodeUtf8(data, size, ERROR_UNKNOWN, errors, consumed);
    }

    public static PyUnicode fromString(String s) {
        int kind = KIND_1BYTE;
        boolean isAscii = true;
        for (int i = 0; i < s.length(); i++) {
            int cp = s.codePointAt(i);
            if (cp > 127) {
                isAscii = false;
                if (cp > 255) {
                    kind = KIND_2BYTE;
                    if (cp > 65535) {
                        kind = KIND_4BYTE;
                        break;
                    }
                }
            }
        }
        final byte[] result = new byte[(kind == KIND_4BYTE ? s.codePointCount(0, s.length()) : s.length()) << kind];
        if (kind == KIND_1BYTE) {
            for (int i = 0; i < s.length(); i++) {
                result[i] = (byte)s.charAt(i);
            }
        } else if (kind == KIND_2BYTE) {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                result[i << 1] = (byte)(ch >> 8);
                result[(i << 1) + 1] = (byte)ch;
            }
        } else {
            for (int i = 0, j = 0; j < result.length; j += 4) {
                int cp = s.codePointAt(i);
                result[j] = (byte)(cp >>> 24);
                result[j + 1] = (byte)(cp >> 16);
                result[j + 2] = (byte)(cp >> 8);
                result[j + 3] = (byte)cp;
                i += Character.charCount(cp);
            }
        }
        return new PyUnicode(result, FLAG_COMPACT | kind | (isAscii ? FLAG_ASCII : 0));
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
        return new PyUnicode(result, FLAG_COMPACT | (utf16 ? KIND_2BYTE : KIND_1BYTE) | (isAscii ? FLAG_ASCII : 0));
    }

    public static PyUnicode fromCodepointArray(int[] s) {
        int kind = KIND_1BYTE;
        boolean isAscii = true;
        for (int i = 0; i < s.length; i++) {
            if (s[i] > 127) {
                isAscii = false;
                if (s[i] > 255) {
                    kind = KIND_2BYTE;
                    if (s[i] > 65535) {
                        kind = KIND_4BYTE;
                        break;
                    }
                }
            }
        }
        final byte[] result = new byte[s.length << kind];
        if (kind == KIND_1BYTE) {
            for (int i = 0; i < s.length; i++) {
                result[i] = (byte)s[i];
            }
        } else if (kind == KIND_2BYTE) {
            for (int i = 0; i < s.length; i++) {
                result[i << 1] = (byte)(s[i] >>> 8);
                result[(i << 1) + 1] = (byte)s[i];
            }
        } else {
            for (int i = 0; i < s.length; i++) {
                result[i << 2] = (byte)(s[i] >>> 24);
                result[(i << 2) + 1] = (byte)(s[i] >>> 16);
                result[(i << 2) + 2] = (byte)(s[i] >>> 8);
                result[(i << 2) + 3] = (byte)s[i];
            }
        }
        return new PyUnicode(result, FLAG_COMPACT | kind | (isAscii ? FLAG_ASCII : 0));
    }

    @Override
    public boolean __bool__() {
        return data.length > 0;
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
        while (begin < end) {
            if ((data[begin++] & 0x80) != 0) {
                return 255;
            }
        }
        return 127;
    }

    private static PyUnicode unicodeChar(int ch) {
        assert ch <= MAX_UNICODE;

        if (ch < 256) {
            return GlobalStrings.latin1(ch);
        }

        final PyUnicode result = fromSizeAndMax(1, ch);
        assert result.getKind() != KIND_1BYTE;
        if (result.getKind() == KIND_2BYTE) {
            result.data[0] = (byte)(ch >> 8);
            result.data[1] = (byte)ch;
        } else {
            result.data[0] = (byte)(ch >> 24);
            result.data[1] = (byte)(ch >> 16);
            result.data[2] = (byte)(ch >> 8);
            result.data[3] = (byte)ch;
        }
        return result;
    }

    private static int getShort(byte[] b, int i) {
        i <<= 1;
        return (b[i] & 0xff) << 8 | (b[i + 1] & 0xff);
    }

    private static PyUnicode fromUCS2(byte[] u, int size) {
        if (size == 0) {
            return GlobalStrings.empty;
        }
        assert size > 0;
        if (size == 1) {
            return unicodeChar(getShort(u, 0));
        }

        int maxChar = ucs2FindMaxChar(u, 0, size);
        PyUnicode result = fromSizeAndMax(size, maxChar);
        if (maxChar >= 256) {
            System.arraycopy(u, 0, result.data, 0, size << 1);
        } else {
            deflate(u, result.data, 0, u.length, KIND_2BYTE, KIND_1BYTE);
        }
        return result;
    }

    private static int ucs2FindMaxChar(byte[] data, int begin, int end) {
        int max = 127;
        while (begin < end) {
            if (getShort(data, begin++) > max) {
                if (max == 0xff) {
                    return 0xffff;
                }
                max = 0xff;
            }
        }
        return max;
    }

    private static int getInt(byte[] b, int i) {
        i <<= 2;
        return (b[i] & 0xff) << 24 | (b[i + 1] & 0xff) << 16 | (b[i + 2] & 0xff) << 8 | (b[i + 3] & 0xff);
    }

    private static PyUnicode fromUCS4(byte[] u, int size) {
        if (size == 0) {
            return GlobalStrings.empty;
        }
        assert size > 0;
        if (size == 1) {
            return unicodeChar(getInt(u, 0));
        }

        int maxChar = ucs4FindMaxChar(u, 0, size);
        PyUnicode result = fromSizeAndMax(size, maxChar);
        if (maxChar < 256) {
            deflate(u, result.data, 0, u.length, KIND_4BYTE, KIND_1BYTE);
        } else if (maxChar < 0x10000) {
            deflate(u, result.data, 0, u.length, KIND_4BYTE, KIND_2BYTE);
        } else {
            System.arraycopy(u, 0, result.data, 0, size << 2);
        }
        return result;
    }

    private static int ucs4FindMaxChar(byte[] data, int begin, int end) {
        int max = 127;
        while (begin < end) {
            if (getShort(data, begin++) > max) {
                if (max == 0xffff) {
                    return MAX_UNICODE;
                }
                if (max == 0xff) {
                    max = 0xffff;
                } else {
                    max = 0xff;
                }
            }
        }
        return max;
    }

    private static final byte ERROR_UNKNOWN = 0;
    private static final byte ERROR_STRICT = 1;
    private static final byte ERROR_SURROGATEESCAPE = 2;
    private static final byte ERROR_REPLACE = 3;
    private static final byte ERROR_IGNORE = 4;
    private static final byte ERROR_BACKSLASHREPLACE = 5;
    private static final byte ERROR_SURROGATEPASS = 6;
    private static final byte ERROR_XMLCHARREFREPLACE = 7;
    private static final byte ERROR_OTHER = 8;

    private static byte getErrorHandler(String errors) {
        if (errors == null) {
            return ERROR_STRICT;
        }
        switch (errors) {
            case "strict":
                return ERROR_STRICT;
            case "surrogateescape":
                return ERROR_SURROGATEESCAPE;
            case "replace":
                return ERROR_REPLACE;
            case "ignore":
                return ERROR_IGNORE;
            case "backslashreplace":
                return ERROR_BACKSLASHREPLACE;
            case "surrogatepass":
                return ERROR_SURROGATEPASS;
            case "xmlcharrefreplace":
                return ERROR_XMLCHARREFREPLACE;
            default:
                return ERROR_OTHER;
        }
    }

    private static final class PyUnicodeWriter {
        PyUnicode buffer;
        byte[] data;
        byte kind;
        int maxChar;
        int pos;

        PyUnicodeWriter(PyUnicode buffer) {
            this.buffer = buffer;
            update();
        }

        void update() {
            data = buffer.data;
            kind = buffer.getKind();
            maxChar = buffer.getMaxCharValue();
            if (maxChar < 255) {
                maxChar = 255;
            }
        }

        PyUnicode finish() {
            buffer.kindAndFlags = (byte)(kind | FLAG_COMPACT);
            buffer.data = (pos << kind) == data.length ? data : Arrays.copyOf(data, pos << kind);
            return buffer;
        }

        void prepare(int ch, int length) {
            if (ch > maxChar || length > (data.length >> kind)) {
                byte newKind = ch < 256 ? KIND_1BYTE : ch < 0x10000 ? KIND_2BYTE : KIND_4BYTE;
                int extraAmnt = Math.max(length, 4);
                byte[] newData = new byte[((data.length >> kind) + extraAmnt) << newKind];
                inflate(data, newData, 0, pos << kind, kind, newKind);
                data = newData;
                kind = newKind;
            }
        }

        void prepareKind(byte kind) {
            if (kind > this.kind) {
                byte[] newData = new byte[data.length << kind];
                inflate(data, newData, 0, pos << kind, this.kind, kind);
                data = newData;
                this.kind = kind;
            }
        }

        void writeChar(int ch) {
            assert ch <= MAX_UNICODE;
            prepare(ch, 1);
            putKind(data, pos++, ch, kind);
        }
    }

    private static PyUnicode unicodeDecodeUtf8(byte[] data, int size, byte errorHandler, String errors, int[] consumed) {
        if (size == 0) {
            if (consumed != null) {
                consumed[0] = 0;
            }
            return GlobalStrings.empty;
        }

        if (size == 1 && (data[0] & 0xff) < 128) {
            if (consumed != null) {
                consumed[0] = 1;
            }
            return GlobalStrings.latin1(data[0] & 0xff);
        }

        int s = 0;
        final int end = size;

        final PyUnicode result = fromSizeAndMax(size, 127);
        s += asciiDecode(data, s, end, result.data);
        if (s == end) return result;

        final PyUnicodeWriter writer = new PyUnicodeWriter(result);
        writer.pos = s;

        int startInPos = 0, endInPos = 0;
        String errmsg = "";
        // PyObject errorHandlerObj = null;
        // PyObject exc = null;

        final int[] ptr = new int[2];
        while (s < end) {
            byte kind = writer.kind;
            ptr[0] = s;
            ptr[1] = writer.pos;
            int ch = utf8Decode(data, ptr, end, writer.data, kind);
            s = ptr[0];
            writer.pos = ptr[1];

            switch (ch) {
                case 0:
                    if (s == end || consumed != null) {
                        if (consumed != null) {
                            consumed[0] = s;
                        }
                        return writer.finish();
                    }
                    errmsg = "unexpected end of data";
                    startInPos = s;
                    endInPos = end;
                    break;
                case 1:
                    errmsg = "invalid start byte";
                    startInPos = s;
                    endInPos = startInPos + 1;
                    break;
                case 2:
                    if (consumed != null && (data[s] & 0xff) == 0xED && end - s == 2 && (data[s + 1] & 0xff) >= 0xA0 && (data[s + 1] & 0xff) <= 0xBF) {
                        consumed[0] = s;
                        return writer.finish();
                    }
                case 3:
                case 4:
                    errmsg = "invalid continuation byte";
                    startInPos = s;
                    endInPos = startInPos + 1;
                    break;
                default:
                    writer.writeChar(ch);
                    continue;
            }

            if (errorHandler == ERROR_UNKNOWN) {
                errorHandler = getErrorHandler(errors);
            }

            switch (errorHandler) {
                case ERROR_IGNORE:
                    s += endInPos - startInPos;
                    break;

                case ERROR_REPLACE:
                    writer.writeChar(0xfffd);
                    s += endInPos - startInPos;
                    break;

                case ERROR_SURROGATEESCAPE:
                    writer.prepareKind(KIND_2BYTE);
                    for (int i = startInPos; i < endInPos; i++) {
                        putKind(writer.data, writer.pos++, data[i] + 0xdc00, writer.kind);
                    }
                    s += endInPos - startInPos;

                case ERROR_STRICT:
                    if (startInPos == endInPos - 1) {
                        throw new WrappedPyException(PyException::new, String.format("Can't decode byte 0x%1$02x in position %2$d: %s", data[startInPos] & 0xff, startInPos, errmsg));
                    } else {
                        throw new WrappedPyException(PyException::new, "Can't decode bytes in position " + startInPos + "-" + endInPos + ": " + errmsg);
                    }

                default:
                    throw new UnsupportedOperationException("Unimplemented error handler: " + errorHandler);
            }
        }

        return writer.finish();
    }

    private static int asciiDecode(byte[] data, int start, int end, byte[] dest) {
        int n = 0;
        while (start < end) {
            if (data[start] < 0) {
                break;
            }
            dest[n++] = data[start++];
        }
        return n;
    }

    private static int utf8Decode(byte[] data, int[] ptr, int end, byte[] dest, byte kind) {
        final int maxChar = kind == KIND_1BYTE ? 0xff : kind == KIND_2BYTE ? 0xffff : MAX_UNICODE;
        int s = ptr[0];
        int p = ptr[1];

        while (s < end) {
            int ch = data[s] & 0xff;

            if (ch < 0x80) {
                s++;
                putKind(dest, p++, ch, kind);
                continue;
            }

            if (ch < 0xE0) {
                if (ch < 0xC2) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 1;
                }
                if (end - s < 2) {
                    break;
                }
                int ch2 = data[s + 1] & 0xff;
                if (ch2 < 0x80 || ch2 >= 0xC0) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 2;
                }
                ch = (ch << 6) + ch2 - ((0xC0 << 6) + 0x80);
                assert ch > 0x007F && ch <= 0x07FF;
                s += 2;
                if (maxChar <= 0x007F || (maxChar < 0x07FF && ch > maxChar)) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return ch;
                }
                putKind(dest, p++, ch, kind);
                continue;
            }

            if (ch < 0xF0) {
                if (end - s < 3) {
                    if (end - s < 2) {
                        break;
                    }
                    int ch2 = data[s + 1] & 0xff;
                    if (ch2 < 0x80 || ch2 >= 0xC0 || (ch2 < 0xA0 ? ch == 0xE0 : ch == 0xED)) {
                        ptr[0] = s;
                        ptr[1] = p;
                        return 2;
                    }
                    break;
                }
                int ch2 = data[s + 1] & 0xff;
                int ch3 = data[s + 2] & 0xff;
                if (ch2 < 0x80 || ch2 >= 0xC0) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 2;
                }
                if (ch == 0xE0) {
                    if (ch2 < 0xA0) {
                        ptr[0] = s;
                        ptr[1] = p;
                        return 2;
                    }
                } else if (ch == 0xED && ch2 >= 0xA0) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 2;
                }
                if (ch3 < 0x80 || ch3 >= 0xC0) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 3;
                }
                ch = (ch << 12) + (ch2 << 6) + ch3 - ((0xE0 << 12) + (0x80 << 6) + 0x80);
                assert ch > 0x07FF && ch <= 0xFFFF;
                s += 3;
                if (maxChar <= 0x07FF || (maxChar < 0xFFFF && ch > maxChar)) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return ch;
                }
                putKind(dest, p++, ch, kind);
                continue;
            }

            if (ch < 0xF5) {
                if (end - s < 4) {
                    if (end - s < 2) {
                        break;
                    }
                    int ch2 = data[s + 1] & 0xff;
                    if (ch2 < 0x80 || ch2 >= 0xC0 || (ch2 < 0x90 ? ch == 0xF0 : ch == 0xF4)) {
                        ptr[0] = s;
                        ptr[1] = p;
                        return 2;
                    }
                    if (end - s < 3) {
                        break;
                    }
                    int ch3 = data[s + 2] & 0xff;
                    if (ch3 < 0x80 || ch3 >= 0xC0) {
                        ptr[0] = s;
                        ptr[1] = p;
                        return 3;
                    }
                    break;
                }
                int ch2 = data[s + 1] & 0xff;
                int ch3 = data[s + 2] & 0xff;
                int ch4 = data[s + 3] & 0xff;
                if (ch2 < 0x80 || ch2 >= 0xC0) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 2;
                }
                if (ch == 0xF0) {
                    if (ch2 < 0x90) {
                        ptr[0] = s;
                        ptr[1] = p;
                        return 2;
                    }
                } else if (ch == 0xF4 && ch2 >= 0x90) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 2;
                }
                if (ch3 < 0x80 || ch3 >= 0xC0) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 3;
                }
                if (ch4 < 0x80 || ch4 >= 0xC0) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return 4;
                }
                ch = (ch << 18) + (ch2 << 12) + (ch3 << 6) + ch4 - ((0xF0 << 18) + (0x80 << 12) + (0x80 << 6) + 0x80);
                assert ch > 0xFFFF && ch <= 0x10FFFF;
                s += 4;
                if (maxChar <= 0xFFFF || (maxChar < 0x10FFFF && ch > maxChar)) {
                    ptr[0] = s;
                    ptr[1] = p;
                    return ch;
                }
                putKind(dest, p++, ch, kind);
                continue;
            }
            ptr[0] = s;
            ptr[1] = p;
            return 1;
        }
        ptr[0] = s;
        ptr[1] = p;
        return 0;
    }

    private static void putKind(byte[] data, int pos, int val, byte kind) {
        pos <<= kind;
        switch (kind) {
            case KIND_1BYTE:
                data[pos] = (byte)val;
                break;
            case KIND_2BYTE:
                data[pos] = (byte)(val >> 8);
                data[pos + 1] = (byte)val;
                break;
            case KIND_4BYTE:
                data[pos] = (byte)(val >> 24);
                data[pos + 1] = (byte)(val >> 16);
                data[pos + 2] = (byte)(val >> 8);
                data[pos + 3] = (byte)val;
                break;
            default:
                throw new AssertionError();
        }
    }

    private static int getKind(byte[] data, int pos, byte kind) {
        pos <<= kind;
        switch (kind) {
            case KIND_1BYTE:
                return data[pos] & 0xff;
            case KIND_2BYTE:
                return (data[pos] & 0xff) << 8 | data[pos + 1] & 0xff;
            case KIND_4BYTE:
                return (data[pos] & 0xff) << 24 | (data[pos + 1] & 0xff) << 16 | (data[pos + 2] & 0xff) << 8 | data[pos + 3] & 0xff;
            default:
                throw new AssertionError();
        }
    }

    private static boolean isSurrogate(int ch) {
        return 0xD800 <= ch && ch <= 0xDFFF;
    }
}
