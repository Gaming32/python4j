package io.github.gaming32.python4j.objects;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PyDict extends PyObject implements SupportsToArray {
    private final Map<PyObject, PyObject> elements; // TODO: reimplement without Java collections

    private PyDict() {
        elements = new LinkedHashMap<>();
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
    public String __repr__() {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<PyObject, PyObject> entry : elements.entrySet()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(entry.getKey().__repr__());
            sb.append(": ");
            sb.append(entry.getValue().__repr__());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public PyObject[] toArray() {
        return elements.keySet().toArray(PyObject[]::new);
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
}
