package io.github.gaming32.python4j.objects;

import java.lang.reflect.Method;
import java.util.Arrays;

import io.github.gaming32.python4j.pycfile.MarshalReader;
import io.github.gaming32.python4j.runtime.PyFrame;
import io.github.gaming32.python4j.runtime.annotation.PyMethodInfo;

public final class PyWrappedException extends RuntimeException {
    private final PyBaseException wrapped;

    public PyWrappedException(PyBaseException wrapped) {
        super(wrapped.__str__());
        this.wrapped = wrapped;
    }

    public PyBaseException getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return getPythonTraceback();
    }

    public String getPythonTraceback() {
        final StringBuilder result = new StringBuilder("Traceback (most recent call last):\n");
        final StackTraceElement[] stackTrace = getStackTrace();
        stackWalker: for (int i = stackTrace.length - 1; i >= 0; i--) {
            final StackTraceElement frame = stackTrace[i];
            if (frame.getClassName().startsWith("io.github.gaming32.python4j.")) {
                continue;
            }
            String fileName = frame.getFileName();
            String functionName = frame.getMethodName();
            try {
                final Class<?> clazz = PyFrame.PYTHON_CLASSES_BY_NAME.get(frame.getClassName());
                final MarshalReader codeStructure = PyFrame.getCachedCode(clazz);
                if (codeStructure != null) {
                    fileName = ((PyCodeObject)codeStructure.getRef(0)).getCo_filename().toString();
                    for (final Method method : clazz.getDeclaredMethods()) {
                        if (!method.getName().equals(functionName)) continue;
                        if (
                            functionName.equals("main") &&
                            Arrays.equals(method.getParameterTypes(), new Class[] { String[].class })
                        ) continue stackWalker;
                        final PyMethodInfo anno = method.getAnnotation(PyMethodInfo.class);
                        if (anno == null) continue;
                        functionName = ((PyCodeObject)codeStructure.getRef(anno.codeRefId())).getCo_name().toString();
                        break;
                    }
                }
            } catch (Throwable t) {
                result.append(frame.getFileName());
            }
            result.append("  File \"")
                .append(fileName)
                .append("\", line ")
                .append(frame.getLineNumber())
                .append(", in ")
                .append(functionName)
                .append('\n');
        }
        result.append(wrapped.getClass().getSimpleName());
        if (wrapped.getArgs().length() > 0) {
            result.append(": ")
                .append(wrapped);
        }
        return result.toString();
    }
}
