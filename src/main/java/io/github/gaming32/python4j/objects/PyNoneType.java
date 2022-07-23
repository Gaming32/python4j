package io.github.gaming32.python4j.objects;

public final class PyNoneType extends PyObject {
    public static final PyNoneType PyNone = new PyNoneType();
    private static final PyUnicode REPR = PyUnicode.fromString("None");

    private PyNoneType() {
    }

    @Override
    public PyUnicode __repr__() {
        return REPR;
    }

    @Override
    public boolean __bool__() {
        return false;
    }
}
