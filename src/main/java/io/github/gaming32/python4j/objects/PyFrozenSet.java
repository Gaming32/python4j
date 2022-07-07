package io.github.gaming32.python4j.objects;

public class PyFrozenSet extends PySet {
    public static final PyFrozenSet EMPTY = new PyFrozenSet();

    public static PyFrozenSet empty() {
        return new PyFrozenSet();
    }

    @Override
    public String __repr__() {
        return "frozenset(" + super.__repr__() + ")";
    }
}
