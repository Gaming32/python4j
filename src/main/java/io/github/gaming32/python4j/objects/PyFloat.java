package io.github.gaming32.python4j.objects;

public class PyFloat extends PyObject {
    private final double fval;

    private PyFloat(double fval) {
        this.fval = fval;
    }

    public static PyFloat fromDouble(double fval) {
        return new PyFloat(fval);
    }

    public static double unpack8(byte[] data, boolean le) {
        long bits = 0;
        int pos = le ? 7 : 0;
        final int incr = le ? -1 : 1;

        for (int i = 0; i < 8; i++) {
            bits <<= 8;
            bits |= data[pos] & 0xff;
            pos += incr;
        }

        return Double.longBitsToDouble(bits);
    }

    public static void pack8(double x, byte[] data, boolean le) {
        long bits = Double.doubleToRawLongBits(x);
        int pos = le ? 0 : 7;
        final int incr = le ? 1 : -1;

        for (int i = 0; i < 8; i++) {
            data[pos] = (byte)(bits & 0xff);
            bits >>>= 8;
            pos += incr;
        }
    }

    public double getValue() {
        return fval;
    }

    @Override
    public PyUnicode __repr__() {
        return PyUnicode.fromString(Double.toString(fval));
    }

    @Override
    public boolean __bool__() {
        return fval != 0;
    }

    public PyFloat add(PyFloat other) {
        return fromDouble(fval + other.fval);
    }

    @Override
    public PyObject __add__(PyObject other) {
        if (other instanceof PyFloat) {
            return add((PyFloat)other);
        }
        if (other instanceof PyLong) {
            return fromDouble(fval + ((PyLong)other).toDouble());
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public PyObject __radd__(PyObject other) {
        return __add__(other);
    }

    public PyFloat sub(PyFloat other) {
        return fromDouble(fval - other.fval);
    }

    @Override
    public PyObject __sub__(PyObject other) {
        if (other instanceof PyFloat) {
            return sub((PyFloat)other);
        }
        if (other instanceof PyLong) {
            return fromDouble(fval - ((PyLong)other).toDouble());
        }
        return PyNotImplemented.NotImplemented;
    }

    @Override
    public PyObject __rsub__(PyObject other) {
        if (other instanceof PyFloat) {
            return ((PyFloat)other).sub(this);
        }
        if (other instanceof PyLong) {
            return fromDouble(((PyLong)other).toDouble() - fval);
        }
        return PyNotImplemented.NotImplemented;
    }
}
