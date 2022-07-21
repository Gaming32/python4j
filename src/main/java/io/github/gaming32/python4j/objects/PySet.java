package io.github.gaming32.python4j.objects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PySet extends PyObject implements Iterable<PyObject>, SupportsToArray {
    final Set<PyObject> elements; // TODO: reimplement without Java collections

    PySet() {
        elements = new HashSet<>();
    }

    public static PySet empty() {
        return new PySet();
    }

    public void add(PyObject item) {
        elements.add(item);
    }

    public int length() {
        return elements.size();
    }

    @Override
    public Iterator<PyObject> iterator() {
        return elements.iterator();
    }

    @Override
    public String __repr__() {
        StringBuilder sb = new StringBuilder("{");
        Iterator<PyObject> values = elements.iterator();
        while (values.hasNext()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(values.next().__repr__());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean __bool__() {
        return !elements.isEmpty();
    }

    @Override
    public PyObject[] toArray() {
        return elements.toArray(PyObject[]::new);
    }

    public void update(PyObject sequence) {
        if (!(sequence instanceof SupportsToArray)) {
            throw new IllegalArgumentException("Sequence must implement toArray()");
        }
        if (sequence instanceof PySet) {
            elements.addAll(((PySet)sequence).elements);
        } else {
            Collections.addAll(elements, ((SupportsToArray)sequence).toArray());
        }
    }
}
