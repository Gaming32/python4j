package io.github.gaming32.python4j.pycfile;

import static io.github.gaming32.python4j.pycfile.MarshalConstants.*;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

public class MarshalReader extends FilterInputStream {
    private int depth = 0;
    private List<PyObject> refs = new ArrayList<>();

    public MarshalReader(InputStream in) {
        super(in);
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        final int read = in.read(b, off, len);
        if (read != len && !(read == -1 && len == 0)) {
            throw new MarshalException("marshal data too short");
        }
    }

    public int readByte() throws IOException {
        return in.read();
    }

    public int readNoEof() throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new MarshalException("marshal data too short");
        }
        return b;
    }

    public int readShort() throws IOException {
        int x = readLong() | (readNoEof() << 8);
        x |= -(x & 0x8000);
        return x;
    }

    public int readLong() throws IOException {
        return readNoEof() | (readNoEof() << 8) | (readNoEof() << 16) | (readNoEof() << 24);
    }

    public PyLong readLong64() throws IOException {
        byte[] b = new byte[8];
        readFully(b);
        return PyObjectAccess.getInstance().pyLongFromByteArray(b, 8, true, true);
    }

    public PyLong readPyLong() throws IOException {
        int n = readLong();
        if (n == 0) {
            return PyObjectAccess.getInstance().pyLongNew(0);
        }
        if (n == Integer.MIN_VALUE) {
            throw new MarshalException("PyLong size can't be larger than Integer.MAX_VALUE");
        }

        int size = 1 + (Math.abs(n) - 1) / PyLong_MARSHAL_RATIO;
        int shortsInTopDigit = 1 + (Math.abs(n) - 1) % PyLong_MARSHAL_RATIO;
        PyLong result = PyObjectAccess.getInstance().pyLongNew(size);
        result.setSize(n > 0 ? size : -size);

        int[] digits = PyObjectAccess.getInstance().pyLongGetDigits(result);
        for (int i = 0; i < size - 1; i++) {
            int d = 0;
            for (int j = 0; j < PyLong_MARSHAL_RATIO; j++) {
                int md = readShort();
                if (md < 0 || md >= PyLong_MARSHAL_BASE) {
                    throw new MarshalException("bad marshal data (digit out of range in long)");
                }
                d += md << (PyLong_MARSHAL_SHIFT * j);
            }
            digits[i] = d;
        }

        int d = 0;
        for (int j = 0; j < shortsInTopDigit; j++) {
            int md = readShort();
            if (md < 0 || md >= PyLong_MARSHAL_BASE) {
                throw new MarshalException("bad marshal data (digit out of range in long)");
            }
            if (md == 0 && j == shortsInTopDigit - 1) {
                throw new MarshalException("bad marshal data (unnormalized long data)");
            }
            d += md << (PyLong_MARSHAL_SHIFT * j);
        }

        digits[size - 1] = d;
        return result;
    }

    public double readFloatBin() throws IOException {
        byte[] b = new byte[8];
        readFully(b);
        return PyFloat.unpack8(b, true);
    }

    public double readFloatStr() throws IOException {
        byte[] b = new byte[readByte()];
        readFully(b);
        return Double.parseDouble(new String(b, StandardCharsets.US_ASCII));
    }

    private PyObject readRef(PyObject o, int flag) {
        if (o == null) {
            return null;
        }
        if (flag != 0) {
            refs.add(o);
        }
        return o;
    }

    private PyObject readAscii(int n, int flag) throws IOException {
        if (n == -1) {
            throw new MarshalException("EOF read where object expected");
        }
        byte[] b = new byte[n];
        readFully(b);
        PyObject result = PyUnicode.fromKindAndData(PyUnicode.KIND_1BYTE, b, n);
        readRef(result, flag);
        return result;
    }

    private PyObject readTuple(int n, int flag) throws IOException {
        PyTuple result = PyTuple.fromSize(n);
        readRef(result, flag);
        for (int i = 0; i < n; i++) {
            PyObject value = readObject();
            if (value == null) {
                throw new MarshalException("NULL object in marshal data for tuple");
            }
            result.setItem(i, value);
        }
        return result;
    }

    public PyObject readObject() throws IOException {
        int code = readByte();
        PyObject result = null;

        if (code == -1) {
            throw new MarshalException("EOF read where object expected");
        }

        depth++;

        if (depth > MAX_STACK_DEPTH) {
            depth--;
            throw new MarshalException("recursion limit exceeded");
        }

        int flag = code & FLAG_REF;
        int type = code & ~FLAG_REF;

        switch (type) {
            case TYPE_NULL:
                break;

            case TYPE_NONE:
                result = PyNoneType.PyNone;
                break;

            case TYPE_STOPITER:
                result = new PyStopIteration();
                break;

            case TYPE_ELLIPSIS:
                result = PyEllipsisType.PyEllipsis;
                break;

            case TYPE_FALSE:
                result = PyBool.PyFalse;
                break;

            case TYPE_TRUE:
                result = PyBool.PyTrue;
                break;

            case TYPE_INT:
                result = PyLong.fromInt(readLong());
                readRef(result, flag);
                break;

            case TYPE_INT64:
                result = readLong64();
                readRef(result, flag);
                break;

            case TYPE_LONG:
                result = readPyLong();
                readRef(result, flag);
                break;

            case TYPE_FLOAT:
                result = PyFloat.fromDouble(readFloatStr());
                readRef(result, flag);
                break;

            case TYPE_BINARY_FLOAT:
                result = PyFloat.fromDouble(readFloatBin());
                readRef(result, flag);
                break;

            case TYPE_COMPLEX:
                result = PyComplex.fromDoubles(readFloatStr(), readFloatStr());
                readRef(result, flag);
                break;

            case TYPE_BINARY_COMPLEX:
                result = PyComplex.fromDoubles(readFloatBin(), readFloatBin());
                readRef(result, flag);
                break;

            case TYPE_STRING: {
                byte[] b = new byte[readLong()];
                readFully(b);
                result = PyBytes.fromBytesAndSize(b, b.length);
                readRef(result, flag);
                break;
            }

            case TYPE_ASCII_INTERNED:
                // We don't support Python interning (yet)
            case TYPE_ASCII:
                result = readAscii(readLong(), flag);
                break;

            case TYPE_SHORT_ASCII_INTERNED:
                // We don't support Python interning (yet)
            case TYPE_SHORT_ASCII:
                result = readAscii(readByte(), flag);
                break;

            case TYPE_INTERNED:
                // We don't support Python interning (yet)
            case TYPE_UNICODE: {
                int n = readLong();
                if (n != 0) {
                    byte[] b = new byte[n];
                    readFully(b);
                    result = PyUnicode.fromKindAndData(PyUnicode.KIND_2BYTE, b, n);
                } else {
                    result = PyUnicode.fromSizeAndMax(0, 0);
                }
                readRef(result, flag);
                break;
            }

            case TYPE_SMALL_TUPLE:
                result = readTuple(readByte(), flag);
                break;
            case TYPE_TUPLE:
                result = readTuple(readLong(), flag);
                break;

            case TYPE_LIST: {
                int n = readLong();
                result = PyList.fromSize(n);
                readRef(result, flag);
                for (int i = 0; i < n; i++) {
                    PyObject value = readObject();
                    if (value == null) {
                        throw new MarshalException("NULL object in marshal data for list");
                    }
                    ((PyList) result).setItem(i, value);
                }
                break;
            }

            case TYPE_DICT: {
                result = PyDict.empty();
                readRef(result, flag);
                while (true) {
                    PyObject key = readObject();
                    if (key == null) {
                        break;
                    }
                    PyObject value = readObject();
                    if (value == null) {
                        break;
                    }
                    ((PyDict)result).setItem(key, value);
                }
                break;
            }

            case TYPE_SET:
            case TYPE_FROZENSET: {
                int n = readLong();
                if (n == 0 && type == TYPE_FROZENSET) {
                    result = PyFrozenSet.EMPTY;
                    readRef(result, flag);
                } else {
                    result = type == TYPE_SET ? PySet.empty() : PyFrozenSet.empty();
                    readRef(result, flag);
                }
                for (int i = 0; i < n; i++) {
                    PyObject value = readObject();
                    if (value == null) {
                        throw new MarshalException("NULL object in marshal data for set");
                    }
                    ((PySet)result).add(value);
                }
                break;
            }

            case TYPE_CODE: {
                result = new PyCodeObject();
                readRef(result, flag);

                int argCount = readLong();
                int posOnlyArgCount = readLong();
                int kwOnlyArgCount = readLong();
                int stackSize = readLong();
                int flags = readLong();
                PyObject bytecode = readObject();
                PyObject consts = readObject();
                PyObject names = readObject();
                PyObject localsPlusNames = readObject();
                PyObject localsPlusKinds = readObject();
                PyObject filename = readObject();
                PyObject name = readObject();
                PyObject qualname = readObject();
                int firstLineNo = readLong();
                PyObject lineTable = readObject();
                PyObject exceptionTable = readObject();

                new PyCodeObject.Builder()
                    .filename((PyUnicode)filename)
                    .name((PyUnicode)name)
                    .qualname((PyUnicode)qualname)
                    .flags(flags)
                    .code((PyBytes)bytecode)
                    .firstLineNo(firstLineNo)
                    .lineTable((PyBytes)lineTable)
                    .consts((PyTuple)consts)
                    .names((PyTuple)names)
                    .localsPlusNames((PyTuple)localsPlusNames)
                    .localsPlusKinds((PyBytes)localsPlusKinds)
                    .argCount(argCount)
                    .posOnlyArgCount(posOnlyArgCount)
                    .kwOnlyArgCount(kwOnlyArgCount)
                    .stackSize(stackSize)
                    .exceptionTable((PyBytes)exceptionTable)
                    .buildInto((PyCodeObject)result);
                break;
            }

            case TYPE_REF: {
                int n = readLong();
                if (n < 0 || n >= refs.size()) {
                    throw new MarshalException("bad marshal data (invalid reference)");
                }
                result = refs.get(n);
                if (result == PyNoneType.PyNone) {
                    throw new MarshalException("bad marshal data (invalid reference)");
                }
                break;
            }

            default:
                throw new MarshalException("bad marshal data (unknown type code)");
        }

        depth--;
        return result;
    }
}
