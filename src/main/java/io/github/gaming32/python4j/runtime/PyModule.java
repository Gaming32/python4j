package io.github.gaming32.python4j.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.gaming32.python4j.objects.PyObject;

public interface PyModule {
    String getName();
    PyObject getattr(String name);
    void setattr(String name, PyObject value);
    void delattr(String name);
    String[] dir();

    default String[] all() {
        return null;
    }

    default void importStar(Map<String, PyObject> into) {
        final Map<String, PyObject> result = new LinkedHashMap<>();
        final String[] names = all();
        if (names != null) {
            for (String name : names) {
                result.put(name, getattr(name));
            }
        } else {
            for (String name : dir()) {
                if (name.startsWith("_")) continue;
                result.put(name, getattr(name));
            }
        }
        into.putAll(result); // If an exception occurs, this doesn't happen
    }
}
