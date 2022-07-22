package io.github.gaming32.python4j.runtime.modules;

import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyUnicode;
import io.github.gaming32.python4j.runtime.PyArguments;
import io.github.gaming32.python4j.runtime.javavirtualmodule.JavaVirtualModule;
import io.github.gaming32.python4j.runtime.javavirtualmodule.ModuleMethod;

public final class PyBuiltins extends JavaVirtualModule {
    public PyBuiltins() {
        super("builtins");
    }

    private static String getStringArgOrDefault(PyArguments args, String key, String def) {
        final PyObject valuePy = args.getKwarg(key, PyNoneType.PyNone);
        final String value;
        if (valuePy == PyNoneType.PyNone) {
            value = def;
        } else if (valuePy instanceof PyUnicode) {
            value = valuePy.toString();
        } else {
            throw new IllegalArgumentException(key + " must be None or a string, not " + valuePy.getClass().getSimpleName());
        }
        return value;
    }

    @ModuleMethod
    public static PyObject print(PyArguments args) {
        final String sep = getStringArgOrDefault(args, "sep", " ");
        boolean first = true;
        for (final PyObject arg : args.getArgs()) {
            if (!first) {
                System.out.print(sep);
            }
            System.out.print(arg.__str__());
            first = false;
        }
        System.out.print(getStringArgOrDefault(args, "end", System.lineSeparator()));
        return PyNoneType.PyNone;
    }
}
