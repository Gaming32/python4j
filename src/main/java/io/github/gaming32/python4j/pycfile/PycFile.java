package io.github.gaming32.python4j.pycfile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyObject;

public final class PycFile {
    private static final Map<Integer, String> VERSION_NAME_MAP = Map.ofEntries(
        // Map.entry(20121, "1.5"),
        // Map.entry(20121, "1.5.1"),
        Map.entry(20121, "1.5.2"),
        Map.entry(50428, "1.6"),
        Map.entry(50824, "2.0"),
        Map.entry(50823, "2.0.1"),
        // Map.entry(60202, "2.1"),
        // Map.entry(60202, "2.1.1"),
        Map.entry(60202, "2.1.2"),
        Map.entry(60717, "2.2"),
        // Map.entry(62011, "2.3a0"),
        Map.entry(62021, "2.3a0"),
        Map.entry(62011, "2.3a0"),
        Map.entry(62041, "2.4a0"),
        Map.entry(62051, "2.4a3"),
        Map.entry(62061, "2.4b1"),
        Map.entry(62071, "2.5a0"),
        Map.entry(62081, "2.5a0"),
        Map.entry(62091, "2.5a0"),
        Map.entry(62092, "2.5a0"),
        Map.entry(62101, "2.5b3"),
        Map.entry(62111, "2.5b3"),
        Map.entry(62121, "2.5c1"),
        Map.entry(62131, "2.5c2"),
        Map.entry(62151, "2.6a0"),
        Map.entry(62161, "2.6a1"),
        Map.entry(62171, "2.7a0"),
        Map.entry(62181, "2.7a0"),
        Map.entry(62191, "2.7a0"),
        Map.entry(62201, "2.7a0"),
        Map.entry(62211, "2.7a0"),
        Map.entry(3000, "3000"),
        Map.entry(3010, "3000"),
        Map.entry(3020, "3000"),
        Map.entry(3030, "3000"),
        Map.entry(3040, "3000"),
        Map.entry(3050, "3000"),
        Map.entry(3060, "3000"),
        Map.entry(3061, "3000"),
        Map.entry(3071, "3000"),
        Map.entry(3081, "3000"),
        Map.entry(3091, "3000"),
        Map.entry(3101, "3000"),
        Map.entry(3103, "3000"),
        Map.entry(3111, "3.0a4"),
        Map.entry(3131, "3.0b1"),
        Map.entry(3141, "3.1a1"),
        Map.entry(3151, "3.1a1"),
        Map.entry(3160, "3.2a1"),
        Map.entry(3170, "3.2a2"),
        Map.entry(3180, "3.2a3"),
        Map.entry(3190, "3.3a1"),
        Map.entry(3200, "3.3a1"),
        Map.entry(3210, "3.3a1"),
        Map.entry(3220, "3.3a2"),
        Map.entry(3230, "3.3a4"),
        Map.entry(3250, "3.4a1"),
        Map.entry(3260, "3.4a1"),
        Map.entry(3270, "3.4a1"),
        Map.entry(3280, "3.4a1"),
        Map.entry(3290, "3.4a4"),
        Map.entry(3300, "3.4a4"),
        Map.entry(3310, "3.4rc2"),
        Map.entry(3320, "3.5a1"),
        Map.entry(3330, "3.5b1"),
        Map.entry(3340, "3.5b2"),
        Map.entry(3350, "3.5b3"),
        Map.entry(3351, "3.5.2"),
        Map.entry(3360, "3.6a0"),
        Map.entry(3361, "3.6a1"),
        Map.entry(3370, "3.6a2"),
        Map.entry(3371, "3.6a2"),
        Map.entry(3372, "3.6a2"),
        Map.entry(3373, "3.6b1"),
        Map.entry(3375, "3.6b1"),
        Map.entry(3376, "3.6b1"),
        Map.entry(3377, "3.6b1"),
        Map.entry(3378, "3.6b2"),
        Map.entry(3379, "3.6rc1"),
        Map.entry(3390, "3.7a1"),
        Map.entry(3391, "3.7a2"),
        Map.entry(3392, "3.7a4"),
        Map.entry(3393, "3.7b1"),
        Map.entry(3394, "3.7b5"),
        Map.entry(3400, "3.8a1"),
        Map.entry(3401, "3.8a1"),
        Map.entry(3410, "3.8a1"),
        Map.entry(3411, "3.8b2"),
        Map.entry(3412, "3.8b2"),
        Map.entry(3413, "3.8b4"),
        Map.entry(3420, "3.9a0"),
        Map.entry(3421, "3.9a0"),
        Map.entry(3422, "3.9a0"),
        Map.entry(3423, "3.9a2"),
        Map.entry(3424, "3.9a2"),
        Map.entry(3425, "3.9a2"),
        Map.entry(3430, "3.10a1"),
        Map.entry(3431, "3.10a1"),
        Map.entry(3432, "3.10a2"),
        Map.entry(3433, "3.10a2"),
        Map.entry(3434, "3.10a6"),
        Map.entry(3435, "3.10a7"),
        Map.entry(3436, "3.10b1"),
        Map.entry(3437, "3.10b1"),
        Map.entry(3438, "3.10b1"),
        Map.entry(3439, "3.10b1"),
        Map.entry(3450, "3.11a1"),
        Map.entry(3451, "3.11a1"),
        Map.entry(3452, "3.11a1"),
        Map.entry(3453, "3.11a1"),
        Map.entry(3454, "3.11a1"),
        Map.entry(3455, "3.11a1"),
        Map.entry(3456, "3.11a1"),
        Map.entry(3457, "3.11a1"),
        Map.entry(3458, "3.11a1"),
        Map.entry(3459, "3.11a1"),
        Map.entry(3460, "3.11a1"),
        Map.entry(3461, "3.11a1"),
        Map.entry(3462, "3.11a2"),
        Map.entry(3463, "3.11a3"),
        Map.entry(3464, "3.11a3"),
        Map.entry(3465, "3.11a3"),
        Map.entry(3466, "3.11a4"),
        Map.entry(3467, "3.11a4"),
        Map.entry(3468, "3.11a4"),
        Map.entry(3469, "3.11a4"),
        Map.entry(3470, "3.11a4"),
        Map.entry(3471, "3.11a4"),
        Map.entry(3472, "3.11a4"),
        Map.entry(3473, "3.11a4"),
        Map.entry(3474, "3.11a4"),
        Map.entry(3475, "3.11a5"),
        Map.entry(3476, "3.11a5"),
        Map.entry(3477, "3.11a5"),
        Map.entry(3478, "3.11a5"),
        Map.entry(3479, "3.11a5"),
        Map.entry(3480, "3.11a5"),
        Map.entry(3481, "3.11a5"),
        Map.entry(3482, "3.11a5"),
        Map.entry(3483, "3.11a5"),
        Map.entry(3484, "3.11a5"),
        Map.entry(3485, "3.11a5"),
        Map.entry(3486, "3.11a6"),
        Map.entry(3487, "3.11a6"),
        Map.entry(3488, "3.11a6"),
        Map.entry(3489, "3.11a6"),
        Map.entry(3490, "3.11a6"),
        Map.entry(3491, "3.11a6"),
        Map.entry(3492, "3.11a7"),
        Map.entry(3493, "3.11a7"),
        Map.entry(3494, "3.11a7"),
        Map.entry(3495, "3.11b4"),
        Map.entry(3500, "3.12a1"),
        Map.entry(3501, "3.12a1"),
        Map.entry(3502, "3.12a1"),
        Map.entry(3503, "3.12a1"),
        Map.entry(3504, "3.12a1"),
        Map.entry(3505, "3.12a1"),
        Map.entry(3506, "3.12a1"),
        Map.entry(3507, "3.12a1"),
        Map.entry(3550, "3.13a1")
    );

    private static final int MIN_VERSION_SUPPORTED = 3494; // 3.11a7
    private static final int MAX_VERSION_SUPPORTED = 3495; // 3.11b4

    private final PyCodeObject code;
    private final int version;
    private final int[] metadata;

    private PycFile(MarshalReader in) throws IOException {
        version = in.readShort();
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
        if (in.readByte() != '\r' | in.readByte() != '\n') {
            throw new RuntimeException("Invalid Python bytecode magic");
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
        final String pythonVersion = VERSION_NAME_MAP.get(version);
        if (pythonVersion == null) {
            return Integer.toString(version);
        }
        return version + " (for Python " + pythonVersion + ")";
    }
}
