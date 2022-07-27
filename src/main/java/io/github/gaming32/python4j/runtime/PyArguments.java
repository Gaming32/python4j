package io.github.gaming32.python4j.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyTuple;

// Thanks to @wagyourtail for this idea
public final class PyArguments {
    private final PyObject[] args;
    private final Map<String, PyObject> kwargs;

    public PyArguments(PyObject[] args, Map<String, PyObject> kwargs) {
        this.args = args != null ? args : new PyObject[0];
        this.kwargs = kwargs;
    }

    PyArguments(PyTuple kwNames, PyObject... argsAndKwargs) {
        if (kwNames == null) {
            args = argsAndKwargs;
            kwargs = null;
            return;
        }
        args = new PyObject[argsAndKwargs.length - kwNames.length()];
        System.arraycopy(argsAndKwargs, 0, args, 0, args.length);
        kwargs = new LinkedHashMap<>(kwNames.length() + 1);
        for (int i = args.length; i < argsAndKwargs.length; i++) {
            kwargs.put(kwNames.getItem(i - args.length).toString(), argsAndKwargs[i]);
        }
    }

    public PyObject[] getArgs() {
        return args;
    }

    public Map<String, PyObject> getKwargs() {
        return kwargs;
    }

    public PyObject getArg(int index) {
        return args[index];
    }

    public int getNArgs() {
        return args.length;
    }

    public PyObject getKwarg(String key) {
        if (kwargs == null) return null;
        return kwargs.get(key);
    }

    public PyObject getKwarg(String key, PyObject def) {
        if (kwargs == null) return def;
        return kwargs.getOrDefault(key, def);
    }

    public boolean hasKwarg(String key) {
        if (kwargs == null) return false;
        return kwargs.containsKey(key);
    }

    public int getNKwargs() {
        if (kwargs == null) return 0;
        return kwargs.size();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder("(");
        boolean hasArg = false;
        for (final PyObject arg : args) {
            if (hasArg) {
                result.append(", ");
            }
            result.append(arg.__repr__());
            hasArg = true;
        }
        for (final var entry : kwargs.entrySet()) {
            if (hasArg) {
                result.append(", ");
            }
            result.append(entry.getKey()).append("=").append(entry.getValue().__repr__());
            hasArg = true;
        }
        return result.append(')').toString();
    }
}
