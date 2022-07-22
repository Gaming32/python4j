package io.github.gaming32.python4j.objects;

import java.util.function.Function;

import io.github.gaming32.python4j.runtime.PyArguments;

public final class PyFunctionObject extends PyObject {
    private final Function<PyArguments, PyObject> function;

    public PyFunctionObject(Function<PyArguments, PyObject> function) {
        this.function = function;
    }

    @Override
    public PyObject __call__(PyArguments args) {
        return function.apply(args);
    }
}
