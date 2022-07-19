package io.github.gaming32.python4j.runtime.invoke;

import java.lang.invoke.MethodHandles;

import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyFrame;

public final class CondyBootstraps {
    private CondyBootstraps() {
    }

    public static PyObject constant(MethodHandles.Lookup lookup, String name, Class<PyObject> type, int method, int index) {
        final PyCodeObject methodCo = (PyCodeObject)PyFrame.getCachedCode(lookup.lookupClass()).getRef(method);
        return methodCo.getCo_consts().getItem(index);
    }
}
