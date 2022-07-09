package io.github.gaming32.python4j.runtime.modules;

import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyUnicode;
import io.github.gaming32.python4j.runtime.javavirtualmodule.JavaVirtualModule;
import io.github.gaming32.python4j.runtime.javavirtualmodule.ModuleMethod;
import io.github.gaming32.python4j.runtime.javavirtualmodule.ModuleProperty;

public final class PyBuiltins extends JavaVirtualModule {
    private static String aValue = "hello";

    public PyBuiltins() {
        super("builtins");
    }

    @ModuleMethod
    public static void test(PyObject obj) {
        System.out.println(obj);
    }

    @ModuleProperty.Getter
    public static PyObject getAValue() {
        return PyUnicode.fromString(aValue);
    }

    @ModuleProperty.Setter
    public static void setAValue(PyObject value) {
        aValue = value.toString();
    }

    @ModuleProperty.Deleter
    public static void deleteAValue() {
        aValue = "deleted";
    }
}
