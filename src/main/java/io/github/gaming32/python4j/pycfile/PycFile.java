package io.github.gaming32.python4j.pycfile;

import java.io.IOException;
import java.io.InputStream;

import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyObject;

public final class PycFile {
    private static final int MIN_VERSION_SUPPORTED = 3494; // 3.11a7
    private static final int MAX_VERSION_SUPPORTED = 3495; // 3.11b4

    private final PyCodeObject code;
    private final int version;
    private final int[] metadata;

    private PycFile(MarshalReader in) throws IOException {
        version = in.readShort();
        if (in.readByte() != '\r' | in.readByte() != '\n') {
            throw new RuntimeException("Invalid Python bytecode magic");
        }
        if (version < MIN_VERSION_SUPPORTED) {
            throw new RuntimeException(
                "Python bytecode version " + versionDisplayName(version) +
                " older than minimum supported version " + versionDisplayName(MIN_VERSION_SUPPORTED)
            );
        }
        if (version > MAX_VERSION_SUPPORTED) {
            throw new RuntimeException(
                "Python bytecode version " + versionDisplayName(version) +
                " newer than maximum supported version " + versionDisplayName(MAX_VERSION_SUPPORTED)
            );
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

    public PycFile(PyCodeObject code, int version, int[] metadata) {
        this.code = code;
        this.version = version;
        this.metadata = metadata;
    }

    public static PycFile read(InputStream is) throws IOException {
        return new PycFile(is instanceof MarshalReader ? (MarshalReader)is : new MarshalReader(is));
    }

    public PyCodeObject getCode() {
        return code;
    }

    public int getVersion() {
        return version;
    }

    public int[] getMetadata() {
        return metadata;
    }

    private static String versionDisplayName(int version) {
        final String pythonVersion = VersionInfo.VERSION_NAME_MAP.get(version);
        if (pythonVersion == null) {
            if (version >= 3450) {
                return version + " (for some Python 3." + (version - 2900) / 50 + ")";
            }
            return Integer.toString(version);
        }
        return version + " (for Python " + pythonVersion + ")";
    }
}
