package io.github.gaming32.python4j;

import java.io.IOException;
import java.io.InputStream;

import io.github.gaming32.python4j.bytecode.Bytecode;
import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.pycfile.PycFile;

public class AppTest {
    public static void main(String[] args) throws IOException {
        final Bytecode bytecode;
        try (InputStream is = AppTest.class.getResourceAsStream("/test.cpython-311.pyc")) {
            bytecode = new Bytecode(PycFile.read(is).getCode());
        }
        System.out.println(bytecode.info());
        System.out.println();
        System.out.println(bytecode.dis());
        System.out.println();
        final Bytecode otherBytecode = new Bytecode((PyCodeObject)bytecode.getCodeObj().getCo_consts().getItem(0));
        System.out.println(otherBytecode.info());
        System.out.println();
        System.out.println(otherBytecode.dis());
    }
}
