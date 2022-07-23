package io.github.gaming32.python4j.objects;

public final class PyBool extends PyLong {
    public static final PyBool PyFalse = new PyBool(0);
    public static final PyBool PyTrue = new PyBool(1);
    public static final PyUnicode TRUE_REPR = PyUnicode.fromString("True");
    public static final PyUnicode FALSE_REPR = PyUnicode.fromString("False");

    private PyBool(int v) {
        super(1);
        digits[0] = v;
    }

    @Override
    public PyUnicode __repr__() {
        return digits[0] != 0 ? TRUE_REPR : FALSE_REPR;
    }

    public boolean booleanValue() {
        return __bool__();
    }

    public static PyBool fromBoolean(boolean value) {
        return value ? PyTrue : PyFalse;
    }

    public static PyBool bool(PyObject o) {
        return fromBoolean(o.__bool__());
    }
}
