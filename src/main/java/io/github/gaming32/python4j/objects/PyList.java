package io.github.gaming32.python4j.objects;

import java.util.ArrayList;
import java.util.List;

public class PyList extends PyObject {
    private final List<PyObject> elements; // TODO: reimplement without Java collections

    private PyList(int size) {
        elements = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            elements.add(null);
        }
    }

    public static PyList fromSize(int size) {
        return new PyList(size);
    }

    public void setItem(int index, PyObject item) {
        elements.set(index, item);
    }

    public PyObject getItem(int index) {
        return elements.get(index);
    }

    public int length() {
        return elements.size();
    }

    @Override
    public String __repr__() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements.get(i).__repr__());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean __bool__() {
        return !elements.isEmpty();
    }
}
