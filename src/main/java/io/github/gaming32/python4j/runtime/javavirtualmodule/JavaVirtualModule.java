package io.github.gaming32.python4j.runtime.javavirtualmodule;

import java.lang.invoke.MethodHandles;

import io.github.gaming32.python4j.objects.PyObject;

public abstract class JavaVirtualModule {
    private final String name;
    private String[] all = null;
    final MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected JavaVirtualModule(String name) {
        this.name = name;
    }

    protected void setAll(String... members) {
        all = members;
    }

    protected String getName() {
        return name;
    }

    protected String[] getAll() {
        return all;
    }

    protected void init() {
    }

    protected PyObject getattr(String attr) {
        return null;
    }

    protected boolean setattr(String attr, PyObject value) {
        return false;
    }
}
