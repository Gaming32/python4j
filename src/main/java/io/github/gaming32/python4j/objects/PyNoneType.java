package io.github.gaming32.python4j.objects;

public final class PyNoneType extends PyObject {
    public static final PyNoneType PyNone = new PyNoneType();

    private PyNoneType() {
    }

    @Override
    public String __repr__() {
        return "None";
    }

    @Override
    public boolean __bool__() {
        return false;
    }
}
