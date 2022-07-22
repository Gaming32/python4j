package io.github.gaming32.python4j;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import io.github.gaming32.python4j.bytecode.Disassemble;
import io.github.gaming32.python4j.compile.PythonToJavaCompiler;
import io.github.gaming32.python4j.objects.WrappedPyException;
import io.github.gaming32.python4j.pycfile.PycFile;

public class CompilerTest {
    public static void main(String[] args) throws Throwable {
        final PycFile pycFile;
        try (InputStream is = CompilerTest.class.getResourceAsStream("/simple_test.cpython-311.pyc")) {
            pycFile = PycFile.read(is);
        }
        System.out.println(Disassemble.codeInfo(pycFile.getCode()));
        System.out.println();
        Disassemble.disassemble(pycFile.getCode(), System.out);
        System.out.println();

        final PythonToJavaCompiler compiler = PythonToJavaCompiler.compile("simple_test", pycFile);
        final byte[] result = compiler.getResult().toByteArray();
        CheckClassAdapter.verify(new ClassReader(result), true, new PrintWriter(System.out));
        try (OutputStream os = new FileOutputStream("simple_test.class")) {
            os.write(result);
        }

        try {
            new ClassLoader() {
                public Class<?> loadFromBytecode(String name, byte[] bytecode) {
                    return super.defineClass(name, bytecode, 0, bytecode.length);
                }
            }.loadFromBytecode("simple_test", result)
                .getMethod("main", String[].class)
                .invoke(null, new Object[] {args});
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof WrappedPyException) {
                System.err.println(((WrappedPyException)e.getCause()).getPythonTraceback());
            } else {
                throw e.getCause();
            }
        }
    }
}
