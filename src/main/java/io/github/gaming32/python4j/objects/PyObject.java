package io.github.gaming32.python4j.objects;

import io.github.gaming32.python4j.runtime.PyArguments;

public class PyObject {
    public String __str__() {
        return __repr__();
    }

    public String __repr__() {
        return "<" + getClass().getSimpleName() + " object 0x" + Integer.toHexString(hashCode()) + ">";
    }

    @Override
    public String toString() {
        return __str__();
    }

    public long __hash__() {
        return System.identityHashCode(this);
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
