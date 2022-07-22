package io.github.gaming32.python4j.runtime;

import java.util.function.Function;

import io.github.gaming32.python4j.objects.PyObject;

public final class PySimpleFunctionObject extends PyObject {
    private final Function<PyArguments, PyObject> function;

    public PySimpleFunctionObject(Function<PyArguments, PyObject> function) {
        this.function = function;
    }

    @Override
    public PyObject __call__(PyArguments args) {
        return function.apply(args);
    }
}
