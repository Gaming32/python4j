package io.github.gaming32.python4j.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PyList extends PyObject implements SupportsToArray {
    private final List<PyObject> elements;

    private PyList(int size) {
        elements = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            elements.add(null);
        }
    }

    private PyList(List<PyObject> elements) {
        this.elements = elements;
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
    public PyObject[] toArray() {
        return elements.toArray(new PyObject[elements.size()]);
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
    public PyUnicode __repr__() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements.get(i).__repr__());
        }
        return PyUnicode.fromString(sb.append(']').toString());
    }

    @Override
    public boolean __bool__() {
        return !elements.isEmpty();
    }

    @Override
    public long __hash__() {
        return notHashable();
    }

    public PyList concat(PyList other) {
        final List<PyObject> result = new ArrayList<>(elements.size() + other.elements.size());
        result.addAll(elements);
        result.addAll(other.elements);
        return new PyList(result);
    }

    @Override
    public PyObject __add__(PyObject other) {
        if (!(other instanceof PyList)) {
            throw new WrappedPyException(PyException::new, "TypeError: can only concatenate list (not \"" + other.getClass().getSimpleName() + "\") to list");
        }
        return concat((PyList)other);
    }

    public PyList repeat(int n) {
        if (n == 0) {
            return new PyList(new ArrayList<>());
        }
        if (n == 1) {
            return new PyList(new ArrayList<>(elements));
        }
        final List<PyObject> result = new ArrayList<>(elements.size() * n);
        result.addAll(elements);
        int i = 1;
        while (i << 1 <= n) {
            result.addAll(result);
            i <<= 1;
        }
        while (i < n) {
            result.addAll(elements);
            i++;
        }
        return new PyList(result);
    }

    @Override
    public PyObject __mul__(PyObject other) {
        if (other instanceof PyLong) {
            final int[] longAndOverflow = ((PyLong)other).asLongAndOverflow();
            if (longAndOverflow[1] != 0) {
                throw new IllegalArgumentException("PyLong too large");
            }
            return repeat(longAndOverflow[0]);
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public PyObject __rmul__(PyObject other) {
        return __mul__(other);
    }
}
