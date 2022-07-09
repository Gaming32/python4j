package io.github.gaming32.python4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import io.github.gaming32.python4j.objects.PyUnicode;

public class AppTest {
    public static void main(String[] args) throws IOException {
        System.setProperty("python4j.hashSeed", "12345");
        // final Bytecode bytecode;
        // try (InputStream is = AppTest.class.getResourceAsStream("/test.cpython-311.pyc")) {
        //     bytecode = new Bytecode(PycFile.read(is).getCode());
        // }
        try (Writer out = new FileWriter("test.txt", StandardCharsets.UTF_8)) {
            out.write(
                PyUnicode.fromString("Hello \u0129 ")
                    .concat(PyUnicode.fromString("world 😃!"))
                    .toString()
            );
        }
    }
}
