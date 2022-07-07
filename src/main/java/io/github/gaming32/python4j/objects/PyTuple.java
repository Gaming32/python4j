package io.github.gaming32.python4j.objects;

public class PyTuple extends PyObject {
    private static final PyTuple EMPTY = new PyTuple(new PyObject[0]);

    private final PyObject[] elements;

    private PyTuple(PyObject[] elements) {
        this.elements = elements;
    }

    private PyTuple(int size) {
        elements = new PyObject[size];
    }

    public static PyTuple fromSize(int size) {
        if (size == 0) {
            return EMPTY;
        }
        return new PyTuple(size);
    }

    public void setItem(int index, PyObject item) {
        elements[index] = item;
    }

    public int length() {
        return elements.length;
    }

    @Override
    public String __repr__() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements[i].__repr__());
        }
        if (elements.length == 1) {
            sb.append(',');
        }
        return sb.append(')').toString();
    }
}
