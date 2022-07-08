package io.github.gaming32.python4j.objects;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PyTuple extends PyObject implements Iterable<PyObject> {
    private static final PyTuple EMPTY = new PyTuple(new PyObject[0]);

    private final PyObject[] elements;

    private PyTuple(PyObject[] elements) {
        this.elements = elements;
    }

    private PyTuple(int size) {
        elements = new PyObject[size];
    }

    public static PyTuple fromElements(PyObject... elements) {
        if (elements.length == 0) {
            return EMPTY;
        }
        return new PyTuple(elements);
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

    public PyObject getItem(int index) {
        return elements[index];
    }

    public int length() {
        return elements.length;
    }

    public Iterator<PyObject> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < elements.length;
            }

            @Override
            public PyObject next() {
                try {
                    return elements[index++];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                }
            }
        };
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

    @Override
    public boolean __bool__() {
        return elements.length > 0;
    }
}
