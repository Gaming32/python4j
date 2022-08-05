package io.github.gaming32.python4j.nativeapi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.WString;

import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.pycfile.MarshalReader;
import io.github.gaming32.python4j.pycfile.MarshalWriter;

public final class CPython {
    private static interface PythonLibrary extends Library {
        PythonLibrary INSTANCE = Native.load(Platform.isWindows() ? "python311" : "python3.11", PythonLibrary.class);

        @SuppressWarnings("unused")
        @FieldOrder({"cf_flags", "cf_feature_version"})
        class PyCompilerFlags extends Structure {
            public int cf_flags = 0;
            public int cf_feature_version = 11;
        }

        @SuppressWarnings("unused")
        @FieldOrder({"_type", "func", "err_msg", "exitcode"})
        class PyStatus extends Structure {
            public static class ByValue extends PyStatus implements Structure.ByValue {
                public ByValue() {
                    super();
                }

                public ByValue(Pointer p) {
                    super(p);
                }
            }

            public PyStatus() {
                super();
            }

            public PyStatus(Pointer p) {
                super(p);
            }

            public int _type;
            public Pointer func;
            public Pointer err_msg;
            public int exitcode;
        }

        boolean Py_IsInitialized();
        void PyConfig_InitIsolatedConfig(Pointer config);
        PyStatus.ByValue Py_InitializeFromConfig(Pointer config);
        void Py_SetPath(WString path);
        boolean PyStatus_Exception(PyStatus.ByValue status);
        void Py_ExitStatusException(PyStatus.ByValue status);
        void Py_IncRef(Pointer o);
        void Py_DecRef(Pointer o);
        Pointer PyUnicode_FromStringAndSize(byte[] u, long size);
        Pointer Py_CompileStringObject(byte[] str, Pointer filename, int start, PyCompilerFlags flags, int optimize);
        Pointer PyMarshal_WriteObjectToString(Pointer x, int version);
        long PyBytes_Size(Pointer o);
        Pointer PyBytes_AsString(Pointer o);
    }

    private CPython() {
    }

    public static PyCodeObject compile(String source, String filename) {
        maybeInit();

        final byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        final Pointer filenameCP = PythonLibrary.INSTANCE.PyUnicode_FromStringAndSize(filenameBytes, filenameBytes.length);

        final PythonLibrary.PyCompilerFlags flags = new PythonLibrary.PyCompilerFlags();
        flags.cf_flags = 0x0100 | 0x0800;
        flags.cf_feature_version = -1;
        byte[] sourceBytes = source.getBytes(StandardCharsets.UTF_8);
        sourceBytes = Arrays.copyOf(sourceBytes, sourceBytes.length + 1);
        final Pointer codeCP = PythonLibrary.INSTANCE.Py_CompileStringObject(sourceBytes, filenameCP, 257, flags, 0);

        PythonLibrary.INSTANCE.Py_DecRef(filenameCP);

        final Pointer marshalledCodeCP = PythonLibrary.INSTANCE.PyMarshal_WriteObjectToString(codeCP, MarshalWriter.VERSION);
        PythonLibrary.INSTANCE.Py_DecRef(codeCP);
        final long bytesSize = PythonLibrary.INSTANCE.PyBytes_Size(marshalledCodeCP);
        if (bytesSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Code is too large to be unmarshalled");
        }
        final Pointer marshalledCodePtr = PythonLibrary.INSTANCE.PyBytes_AsString(marshalledCodeCP);

        final byte[] codeBuf = new byte[(int)bytesSize];
        marshalledCodePtr.read(0, codeBuf, 0, codeBuf.length);
        try (MarshalReader reader = new MarshalReader(new ByteArrayInputStream(codeBuf))) {
            return (PyCodeObject)reader.readObject();
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            PythonLibrary.INSTANCE.Py_DecRef(marshalledCodeCP);
        }
    }

    private static void maybeInit() {
        if (!PythonLibrary.INSTANCE.Py_IsInitialized()) {
            // Don't use Memory, since that frees the memory with the instance, which may cause issues
            // 512 bytes Should be plenty of space.
            final Pointer config = new Pointer(Native.malloc(512));
            PythonLibrary.INSTANCE.PyConfig_InitIsolatedConfig(config);
            try {
                PythonLibrary.INSTANCE.Py_SetPath(new WString(
                    new File(
                        CPython.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
                    ).toString()
                ));
            } catch (URISyntaxException e) {
                throw new Error(e);
            }
            final var status = PythonLibrary.INSTANCE.Py_InitializeFromConfig(config);
            if (PythonLibrary.INSTANCE.PyStatus_Exception(status)) {
                PythonLibrary.INSTANCE.Py_ExitStatusException(status);
            }
        }
    }
}
