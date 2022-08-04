package io.github.gaming32.python4j;

import io.github.gaming32.python4j.compile.PythonInterface;
import io.github.gaming32.python4j.nativeapi.CPython;
import io.github.gaming32.python4j.objects.PyCodeObject;

public class CPythonTest {
    public static void main(String[] args) {
        final PyCodeObject code = CPython.compile("print('Hello, world!')", "test.py");
        final PythonInterface python = new PythonInterface();
        python.loadAsModule("test", code).init();
    }
}
