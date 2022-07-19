package io.github.gaming32.python4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.github.gaming32.python4j.bytecode.Disassemble;
import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.pycfile.MarshalReader;
import io.github.gaming32.python4j.pycfile.MarshalWriter;
import io.github.gaming32.python4j.pycfile.PycFile;

public class AppTest {
    public static void main(String[] args) throws IllegalAccessException, IOException {
        final PyCodeObject code;
        try (InputStream is = AppTest.class.getResourceAsStream("/test.cpython-311.pyc")) {
            code = PycFile.read(is).getCode();
        }
        System.out.println(Disassemble.codeInfo(code));
        System.out.println("Code length: " + code.getCo_code().length());
        System.out.println();
        System.out.println();
        final byte[] bytes = MarshalWriter.write(code);
        final PyCodeObject code2 = (PyCodeObject)new MarshalReader(new ByteArrayInputStream(bytes)).readObject();
        System.out.println(Disassemble.codeInfo(code2));
        System.out.println("Code length: " + code2.getCo_code().length());
    }
}
