package io.github.gaming32.python4j.objects;

public class PyStopIteration extends PyException {
    public static final PyStopIteration INSTANCE = new PyStopIteration(PyTuple.fromElements());

    public PyStopIteration(PyTuple args) {
        super(args);
    }
}
