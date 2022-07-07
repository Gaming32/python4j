package io.github.gaming32.python4j.objects;

public abstract class PyVarObject extends PyObject {
    protected int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
