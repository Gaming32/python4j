package io.github.gaming32.python4j.runtime.modules;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyLong;
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

    public PyBuiltins(String name) {
        super(name);
    }

    private static BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 512);
    private static byte[] lineSeparator = System.lineSeparator().getBytes();
    private static byte[] argSeperator = " ".getBytes();

    @ModuleMethod
    public static PyObject print(PyArguments args) throws IOException {
        PyObject[] obj = args.getArgs();
        final byte[] end = args.hasKwarg("end") ? getBytes(args.getKwarg("end", PyNoneType.PyNone)) : lineSeparator;
        if (obj.length > 1) {
            final byte[] sep = args.hasKwarg("sep") ? getBytes(args.getKwarg("sep", PyNoneType.PyNone)) : argSeperator;
            out.write(getBytes(obj[0]));
            for (int i = 1; i < obj.length; i++) {
                out.write(sep);
                out.write(getBytes(obj[i]));
            }
            out.write(end);
        } else if (obj.length > 0) {
            out.write(getBytes(obj[0]));
            out.write(end);
        } else {
            out.write(end);
        }
        out.flush();
        return PyNoneType.PyNone;
    }

    private static byte[] getBytes(PyObject obj) {
        if (Charset.defaultCharset() == StandardCharsets.UTF_8) {
            return obj.__str__().asEncodedString(null, "replace"); // null -> utf-8
        }
        return obj.toString().getBytes();
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

    public static PyUnicode format(PyObject obj, PyUnicode formatSpec) {
        if (formatSpec == null || formatSpec.length() == 0) {
            if (obj.getClass() == PyUnicode.class) {
                return (PyUnicode)obj;
            }
            if (obj.getClass() == PyLong.class) {
                return obj.__str__();
            }
        }

        if (formatSpec == null) {
            formatSpec = PyUnicode.empty();
        }

        return obj.__format__(formatSpec);
    }

    @ModuleMethod
    public static PyObject format(PyArguments args) {
        return format(args.getArg(0), (PyUnicode)args.getArg(1, PyUnicode.empty()));
    }
}
