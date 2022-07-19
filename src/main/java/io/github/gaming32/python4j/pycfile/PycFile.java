package io.github.gaming32.python4j.pycfile;

import java.io.IOException;
import java.io.InputStream;

import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyObject;

public final class PycFile {
    private static final int RAW_MAGIC = 0x0a0d0da6; // Python 3.11a7

    private final PyCodeObject code;
    private final int[] metadata;

    private PycFile(MarshalReader in) throws IOException {
        int magic = in.readLong();
        if (magic != RAW_MAGIC) {
            throw new RuntimeException("Bad magic number in .pyc file");
        }
        metadata = new int[] {
            in.readLong(),
            in.readLong(),
            in.readLong()
        };
        PyObject value = in.readObject();
        if (!(value instanceof PyCodeObject)) {
            throw new RuntimeException("Bad code object in .pyc file");
        }
        code = (PyCodeObject)value;
    }

    public PycFile(PyCodeObject code, int[] metadata) {
        this.code = code;
        this.metadata = metadata;
    }

    public static PycFile read(InputStream is) throws IOException {
        return new PycFile(is instanceof MarshalReader ? (MarshalReader)is : new MarshalReader(is));
    }

    public PyCodeObject getCode() {
        return code;
    }

    public int[] getMetadata() {
        return metadata;
    }
}
