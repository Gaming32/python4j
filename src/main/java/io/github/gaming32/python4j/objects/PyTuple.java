package io.github.gaming32.python4j.objects;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PyTuple extends PyObject implements Iterable<PyObject>, SupportsToArray {
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

    public PyObject[] toArray() {
        return elements.clone();
    }

    @Override
    public PyUnicode __repr__() {
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
        return PyUnicode.fromString(sb.append(')').toString());
    }

    @Override
    public boolean __bool__() {
        return elements.length > 0;
    }

    private static final long XXPRIME_1 = -7046029288634856825L;
    private static final long XXPRIME_2 = -4417276706812531889L;
    private static final long XXPRIME_5 = 2870177450012600261L;

    private long xxRotate(long x) {
        return (x << 31L) | (x >>> 33L);
    }

    @Override
    public long __hash__() {
        long acc = XXPRIME_5;
        for (int i = 0; i < elements.length; i++) {
            long lane = elements[i].__hash__();
            acc += lane * XXPRIME_2;
            acc = xxRotate(acc);
            acc *= XXPRIME_1;
        }

        acc += (long)elements.length ^ (XXPRIME_5 ^ 3527539L);

        if (acc == -1L) {
            return 1546275796L;
        }
        return acc;
    }
}
