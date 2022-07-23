package io.github.gaming32.python4j.objects;

public class PyBytes extends PyVarObject {
    private static final PyBytes EMPTY = new PyBytes(0);
    private static final PyBytes[] CHARACTERS = new PyBytes[256];

    static {
        for (int i = 0; i < 256; i++) {
            CHARACTERS[i] = new PyBytes(new byte[] { (byte)i });
        }
    }

    private long hash = -1;
    private final byte[] bytes;

    private PyBytes(byte[] bytes) {
        this.bytes = bytes;
        size = bytes.length;
    }

    private PyBytes(int size) {
        bytes = new byte[size];
        this.size = size;
    }

    public static PyBytes fromBytesAndSize(byte[] bytes, int size) {
        if (size < 0) {
            throw new RuntimeException("Negative size passed to PyBytes.fromBytesAndSize");
        }
        if (size == 1 && bytes != null) {
            return CHARACTERS[bytes[0] & 0xff];
        }
        if (size == 0) {
            return EMPTY;
        }

        PyBytes result = new PyBytes(size);
        if (bytes == null) {
            return result;
        }
        System.arraycopy(bytes, 0, result.bytes, 0, size);
        return result;
    }

    public static PyBytes fromBytes(byte[] bytes) {
        return fromBytesAndSize(bytes, bytes.length);
    }

    public int length() {
        return bytes.length;
    }

    @Override
    public PyUnicode __repr__() {
        boolean hasSingleQuote = false;
        for (byte b : bytes) {
            if (b == '\'') {
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
        StringBuilder sb = new StringBuilder("b").append(quote);
        for (byte b : bytes) {
            if (b == escapeQuote) {
                sb.append('\\').append(escapeQuote);
            } else if (b == '\n') {
                sb.append("\\n");
            } else if (b == '\r') {
                sb.append("\\r");
            } else if (b == '\t') {
                sb.append("\\t");
            } else if (b == '\f') {
                sb.append("\\f");
            } else if (b == '\b') {
                sb.append("\\b");
            } else if (b >= 32) {
                sb.append((char)b);
            } else {
                sb.append("\\x").append(String.format("%02x", b & 0xff));
            }
        }
        return PyUnicode.fromString(sb.append(quote).toString());
    }

    public byte[] toByteArray() {
        return bytes.clone();
    }

    @Override
    public boolean __bool__() {
        return bytes.length > 0;
    }

    @Override
    public long __hash__() {
        if (hash != -1L) {
            return hash;
        }
        return hash = hash(bytes);
    }


    public static long hash(byte[] bytes) {
        if (bytes.length == 0) {
            return 0L;
        }
        final long h = PyHash.HASH_FUNCTION.getHash().apply(bytes);
        return h == -1L ? -2L : h;
    }
}
