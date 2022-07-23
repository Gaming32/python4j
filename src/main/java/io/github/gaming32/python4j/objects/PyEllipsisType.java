package io.github.gaming32.python4j.objects;

public final class PyEllipsisType extends PyObject {
    public static final PyEllipsisType PyEllipsis = new PyEllipsisType();
    private static final PyUnicode REPR = PyUnicode.fromString("...");

    private PyEllipsisType() {
    }

    @Override
    public PyUnicode __repr__() {
        return REPR;
    }
}
