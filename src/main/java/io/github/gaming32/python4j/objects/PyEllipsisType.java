package io.github.gaming32.python4j.objects;

public final class PyEllipsisType extends PyObject {
    public static final PyEllipsisType PyEllipsis = new PyEllipsisType();

    private PyEllipsisType() {
    }

    @Override
    public String __repr__() {
        return "...";
    }
}
