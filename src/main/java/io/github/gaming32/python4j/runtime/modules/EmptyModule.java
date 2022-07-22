package io.github.gaming32.python4j.runtime.modules;

import java.util.Map;

import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyModule;

public final class EmptyModule implements PyModule {
    private static final String[] EMPTY = new String[0];

    private final String name;

    public EmptyModule(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PyObject getattr(String name) {
        return null;
    }

    @Override
    public void setattr(String name, PyObject value) {
    }

    @Override
    public void delattr(String name) {
    }

    @Override
    public String[] dir() {
        return EMPTY;
    }

    @Override
    public String[] all() {
        return EMPTY;
    }

    @Override
    public void importStar(Map<String, PyObject> into) {
    }
}
