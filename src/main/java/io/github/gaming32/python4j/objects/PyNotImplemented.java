package io.github.gaming32.python4j.objects;

public final class PyNotImplemented extends PyObject {
    public static final PyNotImplemented NotImplemented = new PyNotImplemented();
    private static final PyUnicode REPR = PyUnicode.fromString("NotImplemented");

    private PyNotImplemented() {
    }

    @Override
    public PyUnicode __repr__() {
        return REPR;
    }

    /**
     * @deprecated NotImplemented should not be used in a boolean context
     */
    @Override
    @Deprecated
    public boolean __bool__() {
        System.err.println("DeprecationWarning: NotImplemented should not be used in a boolean context");
        return true;
    }
}
