package io.github.gaming32.python4j.pycfile;

import static io.github.gaming32.python4j.pycfile.MarshalConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyBytes;
import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyComplex;
import io.github.gaming32.python4j.objects.PyDict;
import io.github.gaming32.python4j.objects.PyEllipsisType;
import io.github.gaming32.python4j.objects.PyFloat;
import io.github.gaming32.python4j.objects.PyFrozenSet;
import io.github.gaming32.python4j.objects.PyList;
import io.github.gaming32.python4j.objects.PyLong;
import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyObjectAccess;
import io.github.gaming32.python4j.objects.PySet;
import io.github.gaming32.python4j.objects.PyStopIteration;
import io.github.gaming32.python4j.objects.PyTuple;
import io.github.gaming32.python4j.objects.PyUnicode;

public final class MarshalWriter {
    public static final int VERSION = 4;

    private byte[] buffer = new byte[50];
    private int pos;
    private final Map<PyObject, List<Integer>> map;
    private final int version;
    private MarshalWriter delegate;
    private int depth;

    public MarshalWriter(int version) {
        this.version = version;
        map = version < 3 ? null : new LinkedHashMap<>();
    }

    public static final byte[] write(PyObject obj) {
        return write(obj, VERSION);
    }

    public static final byte[] write(PyObject obj, int version) {
        final MarshalWriter writer = new MarshalWriter(version);
        writer.writeObject(obj);
        return writer.getResult();
    }

    public static final void write(PyObject obj, OutputStream os) throws IOException {
        write(obj, VERSION, os);
    }

    public static final void write(PyObject obj, int version, OutputStream os) throws IOException {
        os.write(write(obj, version));
    }

    private void reserve(int needed) {
        if (pos + needed < buffer.length) return;
        int delta;
        if (buffer.length > 16 * 1024 * 1024) {
            delta = buffer.length >> 3;
        } else {
            delta = buffer.length + 1024;
        }
        delta = Math.max(delta, needed);
        buffer = Arrays.copyOf(buffer, buffer.length + delta);
    }

    public void writeByte(int b) {
        reserve(1);
        buffer[pos++] = (byte)b;
    }

    public void writeBytes(byte[] data) {
        writeBytes(data, 0, data.length);
    }

    public void writeBytes(byte[] data, int off, int count) {
        reserve(count);
        System.arraycopy(data, off, buffer, pos, count);
        pos += count;
    }

    public void writeShort(int s) {
        reserve(2);
        buffer[pos++] = (byte)s;
        buffer[pos++] = (byte)(s >> 8);
    }

    public void writeLong(int i) {
        reserve(4);
        buffer[pos++] = (byte)i;
        buffer[pos++] = (byte)(i >> 8);
        buffer[pos++] = (byte)(i >> 16);
        buffer[pos++] = (byte)(i >> 24);
    }

    private void writeLongAt(int i, int pos) {
        buffer[pos++] = (byte)i;
        buffer[pos++] = (byte)(i >> 8);
        buffer[pos++] = (byte)(i >> 16);
        buffer[pos++] = (byte)(i >> 24);
    }

    public void writePBytes(byte[] data) {
        writePBytes(data, 0, data.length);
    }

    public void writePBytes(byte[] data, int off, int count) {
        writeLong(count);
        writeBytes(data, off, count);
    }

    public void writeShortPBytes(byte[] data) {
        writeShortPBytes(data, 0, data.length);
    }

    public void writeShortPBytes(byte[] data, int off, int count) {
        writeByte(count);
        writeBytes(data, off, count);
    }

    public void writePyLong(PyLong obj) {
        writeByte(TYPE_LONG);
        if (obj.getSize() == 0) {
            writeLong(0);
            return;
        }

        final int[] digits = PyObjectAccess.getInstance().pyLongGetDigits(obj);
        int n = Math.abs(obj.getSize());
        int l = (n - 1) * PyLong_MARSHAL_RATIO;
        int d = digits[n - 1];
        assert d != 0;
        do {
            d >>= PyLong_MARSHAL_SHIFT;
            l++;
        } while (d != 0);
        writeLong(obj.getSize() > 0 ? l : -l);

        for (int i = 0; i < n - 1; i++) {
            d = digits[i];
            for (int j = 0; j < PyLong_MARSHAL_RATIO; j++) {
                writeShort(d & PyLong_MARSHAL_MASK);
                d >>= PyLong_MARSHAL_SHIFT;
            }
            assert d == 0;
        }
        d = digits[n - 1];
        do {
            writeShort(d & PyLong_MARSHAL_MASK);
            d >>= PyLong_MARSHAL_SHIFT;
        } while (d != 0);
    }

    public void writeFloatBin(double v) {
        final byte[] b = new byte[8];
        PyFloat.pack8(v, b, true);
        writeBytes(b);
    }

    public void writeFloatStr(double v) {
        writeBytes(Double.toString(v).getBytes(StandardCharsets.US_ASCII));
    }

    public boolean writeRef(PyObject v) {
        if (version < 3 || map == null) {
            return false;
        }

        final var idAddress = map.get(v);
        if (idAddress != null) {
            writeByte(TYPE_REF);
            idAddress.add(pos);
            if (idAddress.size() < 4) {
                reserve(4);
                int refId = 0;
                for (final var idAddress2 : map.values()) {
                    if (idAddress2.size() < 3) continue;
                    for (int i = 2; i < idAddress2.size(); i++) {
                        writeLongAt(refId, idAddress2.get(i));
                    }
                    idAddress2.set(0, refId++);
                }
                buffer[idAddress.get(1)] |= FLAG_REF;
                pos += 4;
            } else {
                writeLong(idAddress.get(0));
            }
            return true;
        } else {
            final List<Integer> entry = new ArrayList<>(2);
            entry.add(-1);
            entry.add(pos);
            map.put(v, entry);
            return false;
        }
    }

    public void writeObject(PyObject obj) {
        depth++;

        if (depth > MAX_STACK_DEPTH) {
            throw new RuntimeException("Marshal write stack exceeded");
        } else if (obj == null) {
            writeByte(TYPE_NULL);
        } else if (obj == PyNoneType.PyNone) {
            writeByte(TYPE_NONE);
        } else if (obj instanceof PyStopIteration) {
            writeByte(TYPE_STOPITER);
        } else if (obj == PyEllipsisType.PyEllipsis) {
            writeByte(TYPE_ELLIPSIS);
        } else if (obj == PyBool.PyFalse) {
            writeByte(TYPE_FALSE);
        } else if (obj == PyBool.PyTrue) {
            writeByte(TYPE_TRUE);
        } else if (!writeRef(obj)) {
            writeComplexObject(obj);
        }

        depth--;
    }

    private void writeComplexObject(PyObject obj) {
        if (obj.getClass() == PyLong.class) {
            final PyLong theLong = (PyLong)obj;
            final int[] longAndOverflow = theLong.asLongAndOverflow();
            if (longAndOverflow[1] != 0) {
                writePyLong(theLong);
            } else {
                writeByte(TYPE_INT);
            }
        } else if (obj.getClass() == PyFloat.class) {
            if (version > 1) {
                writeByte(TYPE_BINARY_FLOAT);
                writeFloatBin(((PyFloat)obj).getValue());
            } else {
                writeByte(TYPE_FLOAT);
                writeFloatStr(((PyFloat)obj).getValue());
            }
        } else if (obj.getClass() == PyComplex.class) {
            if (version > 1) {
                writeByte(TYPE_BINARY_COMPLEX);
                writeFloatBin(((PyComplex)obj).getReal());
                writeFloatBin(((PyComplex)obj).getImag());
            } else {
                writeByte(TYPE_COMPLEX);
                writeFloatStr(((PyComplex)obj).getReal());
                writeFloatStr(((PyComplex)obj).getImag());
            }
        } else if (obj.getClass() == PyBytes.class) {
            writeByte(TYPE_STRING);
            writePBytes(((PyBytes)obj).toByteArray());
        } else if (obj.getClass() == PyUnicode.class) {
            final PyUnicode theUnicode = (PyUnicode)obj;
            if (version >= 4 && theUnicode.isAscii()) {
                final boolean isShort = theUnicode.length() < 256;
                if (isShort) {
                    writeByte(TYPE_SHORT_ASCII);
                    writeShortPBytes(theUnicode.getLatin1());
                } else {
                    writeByte(TYPE_ASCII);
                    writePBytes(theUnicode.getLatin1());
                }
            } else {
                final byte[] encoded = theUnicode.asEncodedString("utf8", "surrogatepass");
                writeByte(TYPE_UNICODE);
                writePBytes(encoded);
            }
        } else if (obj.getClass() == PyTuple.class) {
            final PyTuple theTuple = (PyTuple)obj;
            final int n = theTuple.length();
            if (version >= 4 && n < 256) {
                writeByte(TYPE_SMALL_TUPLE);
                writeByte(n);
            } else {
                writeByte(TYPE_TUPLE);
                writeLong(n);
            }
            for (int i = 0; i < n; i++) {
                writeObject(theTuple.getItem(i));
            }
        } else if (obj.getClass() == PyList.class) {
            final PyList theList = (PyList)obj;
            final int n = theList.length();
            writeByte(TYPE_LIST);
            writeLong(n);
            for (int i = 0; i < n; i++) {
                writeObject(theList.getItem(i));
            }
        } else if (obj.getClass() == PyDict.class) {
            writeByte(TYPE_DICT);
            for (final var entry : ((PyDict)obj).getElements().entrySet()) {
                writeObject(entry.getKey());
                writeObject(entry.getValue());
            }
            writeObject(null);
        } else if (obj.getClass() == PySet.class || obj.getClass() == PyFrozenSet.class) {
            writeByte(obj.getClass() == PyFrozenSet.class ? TYPE_FROZENSET : TYPE_SET);
            final PySet theSet = (PySet)obj;
            final int n = theSet.length();
            writeLong(n);
            final Map<byte[], PyObject> elementRefs = new IdentityHashMap<>(n);
            final List<byte[]> elementDatas = new ArrayList<>(n);
            for (final PyObject element : theSet) {
                final MarshalWriter delegate = getDelegate();
                delegate.writeObject(element);
                final byte[] elementData = delegate.getResult();
                elementDatas.add(elementData);
                elementRefs.put(elementData, element);
            }
            elementDatas.sort(Arrays::compare);
            for (final byte[] elementData : elementDatas) {
                writeObject(elementRefs.get(elementData));
            }
        } else if (obj instanceof PyCodeObject) {
            final PyCodeObject co = (PyCodeObject)obj;
            final PyBytes co_code = co.getCo_code();
            writeByte(TYPE_CODE);
            writeLong(co.getCo_argcount());
            writeLong(co.getCo_posonlyargcount());
            writeLong(co.getCo_kwonlyargcount());
            writeLong(co.getCo_stacksize());
            writeLong(co.getCo_flags());
            writeObject(co_code);
            writeObject(co.getCo_consts());
            writeObject(co.getCo_names());
            writeObject(co.getCo_localsplusnames());
            writeObject(co.getCo_localspluskinds());
            writeObject(co.getCo_filename());
            writeObject(co.getCo_name());
            writeObject(co.getCo_qualname());
            writeLong(co.getCo_firstlineno());
            writeObject(co.getCo_linetable());
            writeObject(co.getCo_exceptiontable());
        } else {
            writeByte(TYPE_UNKNOWN);
            throw new IllegalArgumentException("Unmarshallable type: " + obj.getClass().getName());
        }
    }

    public byte[] getResult() {
        return Arrays.copyOf(buffer, pos);
    }

    public void reset() {
        if (depth != 0) {
            throw new IllegalStateException("Can't reset MarshalWriter while it's writing");
        }
        pos = 0;
        map.clear();
    }

    private MarshalWriter getDelegate() {
        if (delegate == null) {
            delegate = new MarshalWriter(version);
        } else {
            delegate.reset();
        }
        return delegate;
    }
}
