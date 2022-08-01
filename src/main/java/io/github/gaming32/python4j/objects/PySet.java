package io.github.gaming32.python4j.objects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PySet extends PyObject implements Iterable<PyObject>, SupportsToArray, HashableAnyway {
    final Set<PyObject> elements;

    PySet() {
        elements = new HashSet<>();
    }

    PySet(PySet other) {
        elements = new HashSet<>(other.elements);
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
    public PyUnicode __repr__() {
        StringBuilder sb = new StringBuilder("{");
        Iterator<PyObject> values = elements.iterator();
        while (values.hasNext()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(values.next().__repr__());
        }
        return PyUnicode.fromString(sb.append("}").toString());
    }

    @Override
    public boolean __bool__() {
        return !elements.isEmpty();
    }

    @Override
    public PyObject[] toArray() {
        return elements.toArray(PyObject[]::new);
    }

    @Override
    public long __hash__() {
        return notHashable();
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

    @Override
    public PyObject __or__(PyObject other) {
        if (other instanceof PySet) {
            PySet result = new PySet(this);
            result.elements.addAll(((PySet)other).elements);
            return result;
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public PyObject __and__(PyObject other) {
        if (other instanceof PySet) {
            final PySet result = new PySet();
            for (final PyObject otherElement : ((PySet)other).elements) {
                if (elements.contains(otherElement)) {
                    result.elements.add(otherElement);
                }
            }
            return result;
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public PyObject __sub__(PyObject other) {
        if (other instanceof PySet) {
            final PySet result = new PySet(this);
            for (final PyObject otherElement : ((PySet)other).elements) {
                result.elements.remove(otherElement);
            }
            return result;
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public PyObject __xor__(PyObject other) {
        if (other instanceof PySet) {
            final PySet result = new PySet();
            final Set<PyObject> otherElements = ((PySet)other).elements;
            for (final PyObject element : elements) {
                if (!otherElements.contains(element)) {
                    result.elements.add(element);
                }
            }
            for (final PyObject element : otherElements) {
                if (!elements.contains(element)) {
                    result.elements.add(element);
                }
            }
            return result;
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public long hashAnyway() {
        long hash = 0;
        for (PyObject element : elements) {
            hash ^= shuffleBits(element.__hash__());
        }

        hash ^= ((long)elements.size() + 1L) * 1927868237L;

        hash ^= (hash >>> 11) ^ (hash >>> 25);
        hash = hash * 69069L + 907133923L;

        if (hash == -1) {
            hash = 590923713L;
        }

        return hash;
    }

    private static long shuffleBits(long h) {
        return ((h ^ 89869747L) ^ (h << 16)) * 3644798167L;
    }
}
