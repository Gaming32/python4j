package io.github.gaming32.python4j;

import java.io.File;

import io.github.gaming32.python4j.compile.PythonInterface;
import io.github.gaming32.python4j.nativeapi.CPython;
import io.github.gaming32.python4j.objects.PyCodeObject;

public class CPythonTest {
    public static void main(String[] args) {
        System.setProperty("jna.library.path", "pythonlibs" + File.pathSeparatorChar + System.getProperty("jna.library.path", ""));
        System.setProperty("jna.debug_load", "true");
        final PyCodeObject code = CPython.compile("print('Hello, world!')", "test.py");
        final PythonInterface python = new PythonInterface();
        python.loadAsModule("test", code).init();
    }
}
