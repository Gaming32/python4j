package io.github.gaming32.python4j.objects;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PySet extends PyObject {
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
}
