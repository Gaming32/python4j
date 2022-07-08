package io.github.gaming32.python4j.objects;

public final class PyBool extends PyLong {
    public static final PyBool PyFalse = new PyBool(0);
    public static final PyBool PyTrue = new PyBool(1);

    private PyBool(int v) {
        super(1);
        digits[0] = v;
    }

    @Override
    public String __repr__() {
        return digits[0] != 0 ? "True" : "False";
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
