package io.github.gaming32.python4j.runtime.modules;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyNotImplemented;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyUnicode;
import io.github.gaming32.python4j.runtime.PyArguments;
import io.github.gaming32.python4j.runtime.javavirtualmodule.JavaVirtualModule;
import io.github.gaming32.python4j.runtime.javavirtualmodule.ModuleConstant;
import io.github.gaming32.python4j.runtime.javavirtualmodule.ModuleMethod;

public final class PyBuiltins extends JavaVirtualModule {
    @ModuleConstant
    public static final PyNoneType None = PyNoneType.PyNone;
    @ModuleConstant
    public static final PyNotImplemented NotImplemented = PyNotImplemented.NotImplemented;
    @ModuleConstant
    public static final PyBool True = PyBool.PyTrue;
    @ModuleConstant
    public static final PyBool False = PyBool.PyFalse;

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

    private static BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 512);

    @ModuleMethod
    public static PyObject print(PyArguments args) throws IOException {
        PyObject[] obj = args.getArgs();
        if (obj.length > 1) {
            final String sep = getStringArgOrDefault(args, "sep", " ");
            final StringBuilder sb = new StringBuilder(obj[0].__str__().toString());
            for (int i = 1; i < obj.length; i++) {
                sb.append(sep).append(obj[i].__str__());
            }
            sb.append(getStringArgOrDefault(args, "end", System.lineSeparator()));
            out.write(sb.toString().getBytes());
        } else if (obj.length > 0) {
            out.write(new StringBuilder(obj[0].__str__().toString()).append(getStringArgOrDefault(args, "end", System.lineSeparator())).toString().getBytes());
        } else {
            out.write(getStringArgOrDefault(args, "end", System.lineSeparator()).getBytes());
        }
        out.flush();
        return PyNoneType.PyNone;
    }

    public static String repr(PyObject obj) {
        return obj.__repr__().toString();
    }

    @ModuleMethod
    public static PyObject repr(PyArguments args) {
        return args.getArg(0).__repr__();
    }

    @ModuleMethod
    public static PyObject divmod(PyArguments args) {
        return PyOperator.pyDivmod(args);
    }
}
