package io.github.gaming32.python4j.compile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public final class AsmUtil {
    private AsmUtil() {
    }

    public static int[] getPycMetadata(ClassReader reader) {
        final int[] metadata = new int[3];
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (!descriptor.equals(PythonToJavaCompiler.C_PYCLASSINFO)) {
                    return null;
                }
                return new AnnotationVisitor(api) {
                    @Override
                    public void visit(String name, Object value) {
                        if (name.equals("metadata")) {
                            System.arraycopy(value, 0, metadata, 0, 3);
                        }
                    }
                };
            }
        }, ClassReader.SKIP_CODE);
        return metadata;
    }
}
