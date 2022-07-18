package io.github.gaming32.python4j;

import io.github.gaming32.python4j.objects.PyUnicode;
import io.github.gaming32.python4j.runtime.PyModule;
import io.github.gaming32.python4j.runtime.javavirtualmodule.PyJavaVirtualModule;

public class AppTest {
    public static void main(String[] args) throws IllegalAccessException {
        final PyModule builtins = PyJavaVirtualModule.getVirtualModules().get("builtins");
        System.out.println(builtins.getattr("aValue"));
        builtins.setattr("aValue", PyUnicode.fromString("world"));
        System.out.println(builtins.getattr("aValue"));
        builtins.delattr("aValue");
        System.out.println(builtins.getattr("aValue"));
    }
}
