package io.github.gaming32.python4j.compile;

import java.util.HashMap;
import java.util.Map;

import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.runtime.PyModule;

public final class PythonInterface {
    private static class PythonClassLoader extends ClassLoader {
        Class<?> classFromBytes(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    // private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    private final PythonClassLoader classLoader = new PythonClassLoader();
    private final Map<Map.Entry<String, PyCodeObject>, Class<? extends PyModule>> moduleCache = new HashMap<>();

    public PythonInterface() {
    }

    @SuppressWarnings("unchecked")
    public Class<? extends PyModule> compileAsModule(String moduleName, PyCodeObject code) {
        return moduleCache.computeIfAbsent(Map.entry(moduleName, code), key -> {
            final PythonToJavaCompiler compilation = PythonToJavaCompiler.compileModule(key.getKey(), key.getValue());
            return (Class<? extends PyModule>)classLoader.classFromBytes(
                compilation.getClassName(),
                compilation.getResult().toByteArray()
            );
        });
    }

    public PyModule loadAsModule(String moduleName, PyCodeObject code) {
        try {
            return compileAsModule(moduleName, code).getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
