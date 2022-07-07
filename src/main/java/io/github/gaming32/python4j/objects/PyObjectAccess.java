package io.github.gaming32.python4j.objects;

public final class PyObjectAccess {
    private static final PyObjectAccess INSTANCE = new PyObjectAccess();

    public static final int PyLong_SHIFT = PyLong.SHIFT;

    public static PyObjectAccess getInstance() {
        return INSTANCE;
    }

    public PyLong pyLongNew(int size) {
        return new PyLong(size);
    }

    public int[] pyLongGetDigits(PyLong pyLong) {
        return pyLong.digits;
    }

    public PyLong pyLongFromByteArray(byte[] bytes, int n, boolean littleEndian, boolean signed) {
        return PyLong.fromByteArray(bytes, n, littleEndian, signed);
    }
}
