package io.github.gaming32.python4j.objects;

import java.util.HashMap;
import java.util.Map;

public class PyDict extends PyObject {
    private final Map<PyObject, PyObject> elements; // TODO: reimplement without Java collections

    private PyDict() {
        elements = new HashMap<>();
    }

    public static PyDict empty() {
        return new PyDict();
    }

    public void setItem(PyObject key, PyObject value) {
        elements.put(key, value);
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
}
