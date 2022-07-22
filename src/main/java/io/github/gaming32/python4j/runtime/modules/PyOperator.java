package io.github.gaming32.python4j.runtime.modules;

import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyArguments;
import io.github.gaming32.python4j.runtime.javavirtualmodule.JavaVirtualModule;
import io.github.gaming32.python4j.runtime.javavirtualmodule.ModuleMethod;

public final class PyOperator extends JavaVirtualModule {
    public PyOperator() {
        super("_operator");
    }

    @ModuleMethod("truthy")
    public static PyObject pyTruthy(PyArguments args) {
        return PyBool.fromBoolean(truthy(args.getArg(0)));
    }

    public static boolean truthy(PyObject obj) {
        return obj.__bool__();
    }
}
