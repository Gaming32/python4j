package io.github.gaming32.python4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.github.gaming32.python4j.bytecode.Bytecode;
import io.github.gaming32.python4j.objects.PyBytes;
import io.github.gaming32.python4j.pycfile.PycFile;

public class AppTest {
    public static void main(String[] args) throws IOException {
        System.setProperty("python4j.hashSeed", "12345");
        final Bytecode bytecode;
        try (InputStream is = AppTest.class.getResourceAsStream("/test.cpython-311.pyc")) {
            bytecode = new Bytecode(PycFile.read(is).getCode());
        }
        System.out.println(
            bytecode.getCodeObj()
                .getCo_filename()
                .__hash__()
        );
        System.out.println(PyBytes.fromBytes(
            bytecode.getCodeObj()
                .getCo_filename()
                .toString()
                .getBytes(StandardCharsets.US_ASCII)
            ).__hash__()
        );
    }
}
