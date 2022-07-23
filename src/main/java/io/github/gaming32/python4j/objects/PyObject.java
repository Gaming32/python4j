package io.github.gaming32.python4j.objects;

import io.github.gaming32.python4j.runtime.PyArguments;

public class PyObject {
    public PyUnicode __str__() {
        return __repr__();
    }

    public PyUnicode __repr__() {
        return PyUnicode.fromString("<" + getClass().getSimpleName() + " object 0x" + Integer.toHexString(hashCode()) + ">");
    }

    @Override
    public String toString() {
        return __str__().toString();
    }

    public long __hash__() {
        return System.identityHashCode(this);
    }

    protected final long notHashable() {
        throw new UnsupportedOperationException("Not a hashable type: " + this.getClass().getSimpleName());
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(__hash__());
    }

    public PyObject __call__(PyArguments args) {
        throw new UnsupportedOperationException();
    }

    public boolean __bool__() {
        return true;
    }
}
