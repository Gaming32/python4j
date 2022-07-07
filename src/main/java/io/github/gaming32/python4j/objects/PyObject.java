package io.github.gaming32.python4j.objects;

public abstract class PyObject {
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
    public int hashCode() {
        return Long.hashCode(__hash__());
    }
}
