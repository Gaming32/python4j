package io.github.gaming32.python4j;

import java.io.IOException;
import java.io.InputStream;

import io.github.gaming32.python4j.pycfile.PycFile;

public class AppTest {
    public static void main(String[] args) throws IOException {
        try (InputStream is = AppTest.class.getResourceAsStream("/test.cpython-311.pyc")) {
            System.out.println(PycFile.read(is).getCode().getCo_consts());
        }
    }
}
