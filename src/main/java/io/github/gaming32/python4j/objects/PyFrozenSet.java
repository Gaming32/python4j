package io.github.gaming32.python4j.objects;

public class PyFrozenSet extends PySet {
    public static final PyFrozenSet EMPTY = new PyFrozenSet();

    private long hash = -1L;

    public static PyFrozenSet empty() {
        return new PyFrozenSet();
    }

    @Override
    public PyUnicode __repr__() {
        return PyUnicode.fromString("frozenset(" + super.__repr__() + ")");
    }

    @Override
    public boolean __bool__() {
        return !elements.isEmpty();
    }

    @Override
    public long __hash__() {
        if (this.hash != -1L) {
            return this.hash;
        }
        return this.hash = hashAnyway();
    }
}
