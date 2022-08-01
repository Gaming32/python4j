package io.github.gaming32.python4j.objects;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PyDict extends PyObject implements SupportsToArray {
    private final Map<PyObject, PyObject> elements;

    private PyDict() {
        elements = new LinkedHashMap<>();
    }

    private PyDict(Map<PyObject, PyObject> elements) {
        this.elements = elements;
    }

    public static PyDict empty() {
        return new PyDict();
    }

    public void setItem(PyObject key, PyObject value) {
        elements.put(key, value);
    }

    public PyObject getItem(PyObject key) {
        return elements.get(key);
    }

    public Map<PyObject, PyObject> getElements() {
        return Collections.unmodifiableMap(elements);
    }

    public int length() {
        return elements.size();
    }

    @Override
    public PyUnicode __repr__() {
        final PyUnicode.Builder result = new PyUnicode.Builder().append('{');
        for (Map.Entry<PyObject, PyObject> entry : elements.entrySet()) {
            if (result.length() > 1) {
                result.append(',').append(' ');
            }
            result.append(entry.getKey().__repr__());
            result.append(':').append(' ');
            result.append(entry.getValue().__repr__());
        }
        return result.append('}').finish();
    }

    @Override
    public PyObject[] toArray() {
        return elements.keySet().toArray(PyObject[]::new);
    }

    @Override
    public long __hash__() {
        return notHashable();
    }

    public void update(PyObject other) {
        if (!(other instanceof PyDict)) {
            throw new IllegalArgumentException("Sequence must implement toArray()");
        }
        elements.putAll(((PyDict)other).elements);
    }

    public void update(PyObject other, boolean raiseOnDuplicate) {
        if (!raiseOnDuplicate) {
            update(other);
            return;
        }
        if (!(other instanceof PyDict)) {
            throw new IllegalArgumentException("Sequence must implement toArray()");
        }
        for (final var entry : ((PyDict)other).elements.entrySet()) {
            if (elements.containsKey(entry.getKey())) {
                throw new IllegalArgumentException("Duplicate key: " + entry.getKey());
            }
            elements.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean __bool__() {
        return !elements.isEmpty();
    }

    public PyDict copy() {
        return new PyDict(new LinkedHashMap<>(elements));
    }

    @Override
    public PyObject __or__(PyObject other) {
        if (!(other instanceof PyDict)) {
            return PyNotImplemented.NotImplemented;
        }
        final PyDict otherDict = (PyDict)other;
        final PyDict result = new PyDict(new LinkedHashMap<>(elements.size() + otherDict.elements.size() + 1));
        result.elements.putAll(elements);
        result.elements.putAll(otherDict.elements);
        return result;
    }
}
