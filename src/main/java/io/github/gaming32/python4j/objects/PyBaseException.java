package io.github.gaming32.python4j.objects;

public class PyBaseException extends PyObject {
    private PyTuple args;

    public PyBaseException(PyTuple args) {
        this.args = args;
    }

    public PyTuple getArgs() {
        return args;
    }

    public void setArgs(PyTuple args) {
        this.args = args;
    }

    @Override
    public String __str__() {
        return args.length() == 0 ? "" : args.length() == 1 ? args.getItem(0).__str__() : args.__str__();
    }

    @Override
    public String __repr__() {
        final StringBuilder result = new StringBuilder(getClass().getSimpleName()).append('(');
        if (args.length() > 0) {
            result.append(args.getItem(0).__repr__());
            for (int i = 1; i < args.length(); i++) {
                result.append(", ").append(args.getItem(i).__repr__());
            }
        }
        return result.append(')').toString();
    }
}
