package io.github.gaming32.python4j;

import java.io.IOException;

import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyUnicode;
import io.github.gaming32.python4j.runtime.PyArguments;
import io.github.gaming32.python4j.runtime.modules.PyBuiltins;

public class AppTest {
    public static void main(String[] args) throws IllegalAccessException, IOException {
        PyUnicode a = PyUnicode.fromString("Hello ");
        PyUnicode b = PyUnicode.fromString("world!");
        PyUnicode c = PyUnicode.fromString(" \ud83d\ude0b!");
        PyBuiltins.print(new PyArguments(new PyObject[] {a.concatMultiple(b, c)}, null));
    }
}
