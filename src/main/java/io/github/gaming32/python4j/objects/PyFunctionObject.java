package io.github.gaming32.python4j.objects;

import java.util.function.Function;

import io.github.gaming32.python4j.runtime.PyArguments;

public final class PyFunctionObject extends PyObject {
    private final PyCodeObject code;
    private final Function<PyObject[], PyObject> actualFunction;

    public PyFunctionObject(PyCodeObject code, Function<PyObject[], PyObject> actualFunction) {
        this.code = code;
        this.actualFunction = actualFunction;
    }

    public PyCodeObject getCode() {
        return code;
    }

    public Function<PyObject[], PyObject> getActualFunction() {
        return actualFunction;
    }

    @Override
    public PyObject __call__(PyArguments args) {
        if (args.getNArgs() < code.getCo_argcount()) {
            throw new IllegalArgumentException("Not enough arguments"); // For now
        }
        return actualFunction.apply(args.getArgs());
    }
}
