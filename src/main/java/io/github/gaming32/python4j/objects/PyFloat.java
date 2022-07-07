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
        int p = 0;
        int incr = 1;
        if (le) {
            p += 7;
            incr = -1;
        }

        int sign = ((data[p] & 0xff) >> 7) & 1;
        int e = ((data[p] & 0xff) & 0x7f) << 4;

        p += incr;

        e |= ((data[p] & 0xff) >> 4) & 0xf;
        int fhi = ((data[p] & 0xff) & 0xf) << 24;
        p += incr;

        if (e == 2047) {
            if (fhi != 0) {
                return sign == 0 ? Double.NaN : -Double.NaN;
            } else {
                return sign == 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            }
        }

        fhi |= (data[p] & 0xff) << 16;
        p += incr;

        fhi |= (data[p] & 0xff) << 8;
        p += incr;

        fhi |= data[p] & 0xff;
        p += incr;

        int flo = (data[p] & 0xff) << 16;
        p += incr;

        flo |= (data[p] & 0xff) << 8;
        p += incr;

        flo |= data[p] & 0xff;

        double x = fhi + flo / 16777216.0;
        x /= 268435456.0;

        if (e == 0) {
            e = -1022;
        } else {
            x += 1.0;
            e -= 1023;
        }
        x = x * Math.pow(2.0, e);

        return sign == 0 ? x : -x;
    }

    @Override
    public String __repr__() {
        return Double.toString(fval);
    }
}
