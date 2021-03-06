package io.github.gaming32.python4j.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PyList extends PyObject implements SupportsToArray {
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

    public PyObject[] toArray() {
        return elements.toArray(PyObject[]::new);
    }

    public void extend(PyObject sequence) {
        if (!(sequence instanceof SupportsToArray)) {
            throw new IllegalArgumentException("Sequence must implement toArray()");
        }
        if (sequence instanceof PyList) {
            elements.addAll(((PyList)sequence).elements);
        } else {
            Collections.addAll(elements, ((SupportsToArray)sequence).toArray());
        }
    }

    public void append(PyObject item) {
        elements.add(item);
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
