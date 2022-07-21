package io.github.gaming32.python4j.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyDict;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.pycfile.MarshalReader;
import io.github.gaming32.python4j.runtime.annotation.PyClassInfo;

public class PyFrame {
    public static final Map<String, Class<?>> PYTHON_CLASSES_BY_NAME = new HashMap<>();
    private static final Map<Class<?>, MarshalReader> CODE_CACHE = new HashMap<>();
    private static final ArrayDeque<PyFrame> STACK = new ArrayDeque<>();

    private final PyCodeObject code;
    private final PyObject[] locals;

    private PyFrame(PyCodeObject codeObj) {
        this.code = codeObj;
        locals = new PyObject[codeObj.getCo_nlocals()];
    }

    public static void push(Class<?> clazz, int codeIndex) {
        STACK.push(new PyFrame((PyCodeObject)getCachedCode(clazz).getRef(codeIndex)));
    }

    public static void pop() {
        STACK.pop();
    }

    public static PyFrame top() {
        return STACK.peek();
    }

    public static MarshalReader getCachedCode(Class<?> clazz) {
        if (clazz == null) return null;
        return CODE_CACHE.computeIfAbsent(clazz, key -> {
            final PyClassInfo anno = key.getAnnotation(PyClassInfo.class);
            if (anno == null) return null;
            PYTHON_CLASSES_BY_NAME.put(key.getName(), key);
            final byte[] input = anno.codeObj().getBytes(StandardCharsets.ISO_8859_1);
            final MarshalReader reader = new MarshalReader(new ByteArrayInputStream(input));
            try {
                reader.readObject();
            } catch (IOException e) {
                throw new IOError(e);
            }
            return reader;
        });
    }

    public static void storeFast(PyObject value, int index) {
        STACK.peek().locals[index] = value;
    }

    public PyDict getLocalsAsDict() {
        final PyDict dict = PyDict.empty();
        for (int i = 0; i < locals.length; i++) {
            dict.setItem(code.getCo_varnames().getItem(i), locals[i]);
        }
        return dict;
    }

    public Map<String, PyObject> getLocalsAsMap() {
        final Map<String, PyObject> result = new HashMap<>();
        for (int i = 0; i < locals.length; i++) {
            result.put(code.getCo_varnames().getItem(i).toString(), locals[i]);
        }
        return result;
    }

    public PyCodeObject getCode() {
        return code;
    }
}
