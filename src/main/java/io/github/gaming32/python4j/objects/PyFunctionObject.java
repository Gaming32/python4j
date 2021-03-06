package io.github.gaming32.python4j.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.github.gaming32.python4j.runtime.PyArguments;

public final class PyFunctionObject extends PyObject {
    private final PyCodeObject code;
    private final Function<PyObject[], PyObject> actualFunction;
    private final PyObject[] defaults;
    private final Map<String, PyObject> kwDefaults;
    private final Map<String, PyObject> annotations;

    private final int firstDefault;
    private final Map<String, Integer> kwPositions;

    public PyFunctionObject(
        PyCodeObject code,
        Function<PyObject[], PyObject> actualFunction,
        PyObject[] defaults,
        Map<String, PyObject> kwDefaults,
        Map<String, PyObject> annotations
    ) {
        this.code = code;
        this.actualFunction = actualFunction;
        this.defaults = defaults;
        this.kwDefaults = kwDefaults;
        this.annotations = annotations;

        firstDefault = code.getCo_argcount() - defaults.length;
        kwPositions = new HashMap<>(code.getCo_argcount() - code.getCo_posonlyargcount() + 1);
        final PyTuple co_varnames = code.getCo_varnames();
        for (int i = code.getCo_posonlyargcount(); i < code.getSumArgCount(); i++) {
            kwPositions.put(co_varnames.getItem(i).toString(), i);
        }
    }

    public PyFunctionObject(
        PyCodeObject code,
        Function<PyObject[], PyObject> actualFunction,
        PyTuple defaults,
        PyDict kwDefaults,
        PyTuple annotations
    ) {
        this.code = code;
        this.actualFunction = actualFunction;
        this.defaults = defaults == null ? new PyObject[0] : defaults.toArray();
        if (kwDefaults != null) {
            this.kwDefaults = new HashMap<>(kwDefaults.length() + 1);
            for (final var entry : kwDefaults.getElements().entrySet()) {
                this.kwDefaults.put(entry.getKey().toString(), entry.getValue());
            }
        } else {
            this.kwDefaults = Collections.emptyMap();
        }
        if (annotations != null) {
            this.annotations = new HashMap<>((annotations.length() >> 1) + 1);
            for (int i = 0; i < annotations.length(); i += 2) {
                this.annotations.put(annotations.getItem(i).toString(), annotations.getItem(i + 1));
            }
        } else {
            this.annotations = null;
        }

        firstDefault = code.getCo_argcount() - this.defaults.length;
        kwPositions = new HashMap<>(code.getSumArgCount() - code.getCo_posonlyargcount() + 1);
        final PyTuple co_varnames = code.getCo_varnames();
        for (int i = code.getCo_posonlyargcount(); i < code.getSumArgCount(); i++) {
            kwPositions.put(co_varnames.getItem(i).toString(), i);
        }
    }

    public PyCodeObject getCode() {
        return code;
    }

    public Function<PyObject[], PyObject> getActualFunction() {
        return actualFunction;
    }

    public PyObject[] getDefaults() {
        return defaults;
    }

    public Map<String, PyObject> getKwDefaults() {
        return kwDefaults;
    }

    public Map<String, PyObject> getAnnotations() {
        return annotations;
    }

    @Override
    public PyObject __call__(PyArguments args) {
        final PyObject[] actualArgs = new PyObject[code.getSumArgCount()];
        System.arraycopy(args.getArgs(), 0, actualArgs, 0, args.getNArgs());
        for (int i = args.getNArgs(); i < code.getCo_argcount(); i++) {
            actualArgs[i] = i < firstDefault ? null : defaults[i - firstDefault];
        }
        putKwargs(actualArgs, kwDefaults);
        putKwargs(actualArgs, args.getKwargs());
        return actualFunction.apply(actualArgs);
    }

    private void putKwargs(PyObject[] args, Map<String, PyObject> kwargs) {
        for (final var entry : kwargs.entrySet()) {
            try {
                args[kwPositions.get(entry.getKey())] = entry.getValue();
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Unknown keyword argument: " + entry.getKey());
            }
        }
    }
}
