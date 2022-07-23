package io.github.gaming32.python4j.objects;

import io.github.gaming32.python4j.FloatInfo;

public class PyLong extends PyVarObject {
    static final int SHIFT = 30;
    private static final int DECIMAL_SHIFT = 9;
    private static final int DECIMAL_BASE = 1000000000;
    private static final int BASE = 1 << SHIFT;
    private static final int MASK = BASE - 1;

    private static final int N_SMALL_NEG_INTS = 5;
    private static final int N_SMALL_POS_INTS = 257;
    private static final PyLong[] SMALL_INTS = new PyLong[N_SMALL_NEG_INTS + N_SMALL_POS_INTS];

    static {
        for (int i = -N_SMALL_NEG_INTS; i < N_SMALL_POS_INTS; i++) {
            final PyLong val = new PyLong(1);
            if (i > 0) {
                val.digits[0] = i;
            } else if (i < 0) {
                val.digits[0] = -i;
                val.size = -1;
            }
            SMALL_INTS[i + N_SMALL_NEG_INTS] = val;
        }
    }

    /**
     * For int multiplication, use the O(N**2) school algorithm unless
     * both operands contain more than KARATSUBA_CUTOFF digits (this
     * being an internal Python int digit, in base BASE).
     */
    private static final int KARATSUBA_CUTOFF = 70;
    private static final int KARATSUBA_SQUARE_CUTOFF = 2 * KARATSUBA_CUTOFF;

    /**
     * For exponentiation, use the binary left-to-right algorithm unless the
     * ^ exponent contains more than HUGE_EXP_CUTOFF bits.  In that case, do
     * (no more than) EXP_WINDOW_SIZE bits at a time.  The potential drawback is
     * that a table of 2**(EXP_WINDOW_SIZE - 1) intermediate results is
     * precomputed.
     */
    private static final int EXP_WINDOW_SIZE = 5;
    private static final int EXP_TABLE_LET = 1 << (EXP_WINDOW_SIZE - 1);

    /**
     * Suppose the exponent has bit length e. All ways of doing this
     * need e squarings. The binary method also needs a multiply for
     * each bit set. In a k-ary method with window width w, a multiply
     * for each non-zero window, so at worst (and likely!)
     * ceiling(e/w). The k-ary sliding window method has the same
     * worst case, but the window slides so it can sometimes skip
     * over an all-zero window that the fixed-window method can't
     * exploit. In addition, the windowing methods need multiplies
     * to precompute a table of small powers.
     *
     * For the sliding window method with width 5, 16 precomputation
     * multiplies are needed. Assuming about half the exponent bits
     * are set, then, the binary method needs about e/2 extra mults
     * and the window method about 16 + e/5.
     *
     * The latter is smaller for e > 53 1/3. We don't have direct
     * access to the bit length, though, so call it 60, which is a
     * multiple of a long digit's max bit length (15 or 30 so far).
     */
    private static final int HUGE_EXP_CUTOFF = 60;

    final int[] digits;

    PyLong(int size) {
        digits = new int[this.size = size != 0 ? size : 1];
    }

    public static PyLong fromInt(int ival) {
        if (isSmallInt(ival)) {
            return getSmallInt(ival);
        }
        if (-MASK <= ival && ival <= MASK) {
            return fromMedium(ival);
        }

        int absIval = ival < 0 ? -ival : ival;
        int t = absIval >> SHIFT >> SHIFT;
        int ndigits = 2;
        while (t != 0) {
            ndigits++;
            t >>= SHIFT;
        }

        PyLong result = new PyLong(ndigits);
        result.size = ival < 0 ? -ndigits : ndigits;
        t = absIval;
        int i = 0;
        while (t != 0) {
            result.digits[i++] = t & MASK;
            t >>= SHIFT;
        }
        return result;
    }

    @Override
    public PyUnicode __repr__() {
        final int sizeA = Math.abs(this.size);
        final boolean negative = this.size < 0;
        final int d = 99;

        final int[] pin = digits, pout = new int[1 + sizeA + sizeA / d];
        int size = 0;
        for (int i = sizeA; --i >= 0;) {
            int hi = pin[i];
            for (int j = 0; j < size; j++) {
                final long z = (long)pout[j] << SHIFT | hi;
                hi = (int)(z / DECIMAL_BASE);
                pout[j] = (int)(z - (long)hi * DECIMAL_BASE);
            }
            while (hi != 0) {
                pout[size++] = hi % DECIMAL_BASE;
                hi /= DECIMAL_BASE;
            }
        }
        if (size == 0) {
            pout[size++] = 0;
        }

        int strlen = (negative ? 2 : 1) + (size - 1) * DECIMAL_SHIFT;
        int tenpow = 10;
        int rem = pout[size - 1];
        while (rem >= tenpow) {
            tenpow *= 10;
            strlen++;
        }
        final byte[] result = new byte[strlen];

        int p = strlen;
        int i;
        for (i = 0; i < size - 1; i++) {
            rem = pout[i];
            for (int j = 0; j < DECIMAL_SHIFT; j++) {
                result[--p] = (byte)('0' + rem % 10);
                rem /= 10;
            }
        }
        rem = pout[i];
        do {
            result[--p] = (byte)('0' + rem % 10);
            rem /= 10;
        } while (rem != 0);

        if (negative) {
            result[--p] = '-';
        }

        return PyUnicode.fromKindAndData(PyUnicode.KIND_1BYTE, result, result.length);
    }

    @Override
    public boolean __bool__() {
        return digits.length != 1 || digits[0] != 0;
    }

    @Override
    public long __hash__() {
        int i = size;
        switch (i) {
            case -1: return digits[0] == 1 ? -1 : -digits[0];
            case 0: return 0;
            case 1: return digits[0];
        }
        int sign = 1;
        long x = 0;
        if (i < 0) {
            sign = -1;
            i = -i;
        }
        while (--i >= 0) {
            x = ((x << SHIFT) & PyHash.MODULUS) | (x >> (PyHash.BITS - SHIFT));
            x += digits[i];
            if (x >= PyHash.MODULUS) {
                x -= PyHash.MODULUS;
            }
        }
        x = x * sign;
        return x == -1 ? -2 : x;
    }

    public double toDouble() {
        if (isMediumValue()) {
            return mediumValue();
        }
        final Number[] x = frexp();
        return x[0].doubleValue() * Math.pow(2, x[1].intValue());
    }

    private static final int[] HALF_EVEN_CORRECTION = {0, -1, -2, 1, 0, -1, 2, 1};
    private static final double EXP2_DBL_MANT_DIG = 9007199254740992.0;

    private Number[] frexp() {
        final int[] xDigits = new int[2 + (FloatInfo.DBL_MANT_DIG + 1) / SHIFT];

        final int aSize = Math.abs(size);
        if (aSize == 0) {
            return new Number[] {Double.valueOf(0), Integer.valueOf(0)};
        }
        int aBits = bitLengthDigit(digits[aSize - 1]);
        aBits = (aSize - 1) * SHIFT + aBits;

        int xSize;
        if (aBits < FloatInfo.DBL_MANT_DIG + 2) {
            final int shiftDigits = (FloatInfo.DBL_MANT_DIG + 2 - aBits) / SHIFT;
            final int shiftBits = (FloatInfo.DBL_MANT_DIG + 2 - aBits) % SHIFT;
            xSize = shiftDigits;
            final int rem = vLshift(xDigits, xSize, digits, aSize, shiftBits);
            xSize += aSize;
            xDigits[xSize++] = rem;
        } else {
            int shiftDigits = (aBits - FloatInfo.DBL_MANT_DIG - 2) / SHIFT;
            final int shiftBits = (aBits - FloatInfo.DBL_MANT_DIG - 2) % SHIFT;
            final int rem = vRshift(xDigits, digits, shiftDigits, aSize - shiftDigits, shiftBits);
            xSize = aSize - shiftDigits;
            if (rem != 0) {
                xDigits[0] |= 1;
            } else while (shiftDigits > 0) {
                if (digits[--shiftDigits] != 0) {
                    xDigits[0] |= 1;
                    break;
                }
            }
        }
        assert 1 <= xSize && xSize <= xDigits.length;

        xDigits[0] += HALF_EVEN_CORRECTION[xDigits[0] & 7];
        double dx = xDigits[--xSize];
        while (xSize > 0) {
            dx = dx * BASE + xDigits[--xSize];
        }

        dx /= 4.0 * EXP2_DBL_MANT_DIG;
        if (dx == 1.0) {
            dx = 0.5;
            aBits++;
        }

        return new Number[] {Double.valueOf(size < 0 ? -dx : dx), Integer.valueOf(aBits)};
    }

    private static int bitLengthDigit(int digit) {
        return 31 - Integer.numberOfLeadingZeros(digit);
    }

    private static int vLshift(int[] z, int zp, int[] a, int m, int d) {
        int carry = 0;

        assert 0 <= d && d < SHIFT;
        for (int i = 0; i < m; i++) {
            final long acc = (long)a[i] << d | carry;
            z[zp + i] = (int)acc & MASK;
            carry = (int)(acc >> SHIFT);
        }
        return carry;
    }

    private static int vRshift(int[] z, int[] a, int ap, int m, int d) {
        int carry = 0;
        final int mask = (1 << d) - 1;

        assert 0 <= d && d < SHIFT;
        for (int i = m; i-- > 0;) {
            final long acc = (long)carry << SHIFT | a[ap + i];
            carry = (int)acc & mask;
            z[i] = (int)(acc >> d);
        }
        return carry;
    }

    private static boolean isSmallInt(int ival) {
        return -N_SMALL_NEG_INTS <= ival && ival <= N_SMALL_POS_INTS;
    }

    private static boolean isSmallInt(long ival) {
        return -N_SMALL_NEG_INTS <= ival && ival <= N_SMALL_POS_INTS;
    }

    private static PyLong getSmallInt(int ival) {
        assert isSmallInt(ival);
        return SMALL_INTS[N_SMALL_NEG_INTS + ival];
    }

    private static boolean isMediumInt(int x) {
        int xPlusMask = x + MASK;
        return xPlusMask < MASK + BASE;
    }

    private static boolean isMediumInt(long x) {
        long xPlusMask = x + MASK;
        return xPlusMask < MASK + BASE;
    }

    private static PyLong fromMedium(int x) {
        assert !isSmallInt(x);
        assert isMediumInt(x);
        PyLong result = new PyLong(1);
        int sign = x < 0 ? -1 : 1;
        int absX = x < 0 ? -x : x;
        result.size = sign;
        result.digits[0] = absX;
        return result;
    }

    private static PyLong fromLarge(long x) {
        assert !isMediumInt(x);

        final long absIval;
        final int sign;
        if (x < 0) {
            absIval = -x;
            sign = -1;
        } else {
            absIval = x;
            sign = 1;
        }
        assert absIval >> SHIFT != 0;
        long t = absIval >> (SHIFT << 1);
        int ndigits = 2;
        while (t != 0) {
            ndigits++;
            t >>= SHIFT;
        }
        final PyLong result = new PyLong(ndigits);
        int p = 0;
        result.size = ndigits * sign;
        t = absIval;
        while (t != 0) {
            result.digits[p++] = (int)(t & MASK);
            t >>= SHIFT;
        }
        return result;
    }

    private static PyLong fromTwoDigits(long x) {
        if (isSmallInt(x)) {
            return getSmallInt((int)x);
        }
        assert x != 0L;
        if (isMediumInt(x)) {
            return fromMedium((int)x);
        }
        return fromLarge(x);
    }

    static PyLong fromByteArray(byte[] bytes, int n, boolean littleEndian, boolean isSigned) {
        if (n == 0) {
            return fromInt(0);
        }

        int idigit = 0;

        int pstartbyte, pendbyte, incr;
        if (littleEndian) {
            pstartbyte = 0;
            pendbyte = n - 1;
            incr = 1;
        } else {
            pstartbyte = n - 1;
            pendbyte = 0;
            incr = -1;
        }

        if (isSigned) {
            isSigned = (bytes[pendbyte] & 0xff) >= 0x80;
        }

        int numSignificantBytes;
        {
            int p = pendbyte;
            int insignificant = isSigned ? 0xff : 0x00;

            int i;
            for (i = 0; i < n; i++, p += incr) {
                if ((bytes[p] & 0xff) != insignificant) {
                    break;
                }
            }
            numSignificantBytes = n - i;
            if (isSigned && numSignificantBytes < n) {
                numSignificantBytes++;
            }
        }

        int nDigits = (numSignificantBytes * 8 + SHIFT - 1) / SHIFT;
        PyLong result = new PyLong(nDigits);

        {
            int carry = 1;
            int accum = 0;
            int accumbits = 0;
            int p = pstartbyte;

            for (int i = 0; i < numSignificantBytes; i++, p += incr) {
                int thisByte = bytes[p] & 0xff;
                if (isSigned) {
                    thisByte = (0xff ^ thisByte) + carry;
                    carry = thisByte >> 8;
                    thisByte &= 0xff;
                }

                accum |= thisByte << accumbits;
                accumbits += 8;
                if (accumbits >= SHIFT) {
                    assert idigit < nDigits;
                    result.digits[idigit++] = accum & MASK;
                    accum >>>= SHIFT;
                    accumbits -= SHIFT;
                    assert accumbits < SHIFT;
                }
            }
            assert accumbits < SHIFT;
            if (accumbits != 0) {
                assert idigit < nDigits;
                result.digits[idigit++] = accum;
            }
        }

        result.size = isSigned ? -idigit : idigit;
        return result.normalize().maybeSmall();
    }

    public int[] asLongAndOverflow() {
        int res = -1;

        int i = size;
        int overflow = 0;
        switch (i) {
            case -1:
                res = -digits[0];
                break;
            case 0:
                res = 0;
                break;
            case 1:
                res = digits[0];
                break;
            default:
                int sign = 1;
                int x = 0;
                if (i < 0) {
                    sign = -1;
                    i = -i;
                }
                while (--i >= 0) {
                    int prev = x;
                    x = x << SHIFT | digits[i];
                    if ((x >> SHIFT) != prev) {
                        overflow = sign;
                        return new int[] {res, overflow};
                    }
                }
                if (x <= Integer.MAX_VALUE) {
                    res = x * sign;
                } else if (sign < 0 && x == -Integer.MIN_VALUE) {
                    res = Integer.MIN_VALUE;
                } else {
                    overflow = sign;
                }
        }
        return new int[] {res, overflow};
    }

    public PyLong add(PyLong b) {
        if (isMediumValue() && b.isMediumValue()) {
            return fromTwoDigits((long)mediumValue() + (long)b.mediumValue());
        }

        final PyLong z;
        if (size < 0) {
            if (b.size < 0) {
                z = add0(this, b);
                z.size = -z.size;
            } else {
                z = sub0(b, this);
            }
        } else {
            if (b.size < 0) {
                z = sub0(this, b);
            } else {
                z = add0(this, b);
            }
        }
        return z;
    }

    @Override
    public PyObject __add__(PyObject other) {
        if (other instanceof PyLong) {
            return add((PyLong)other);
        }
        return PyNotImplemented.NotImplemented;
    }

    public PyLong sub(PyLong b) {
        if (isMediumValue() && b.isMediumValue()) {
            return fromTwoDigits((long)mediumValue() - (long)b.mediumValue());
        }

        final PyLong z;
        if (size < 0) {
            if (b.size < 0) {
                z = sub0(b, this);
            } else {
                z = add0(this, b);
                z.size = -z.size;
            }
        } else {
            if (b.size < 0) {
                z = add0(this, b);
            } else {
                z = sub0(this, b);
            }
        }
        return z;
    }

    @Override
    public PyObject __sub__(PyObject other) {
        if (other instanceof PyLong) {
            return sub((PyLong)other);
        }
        return PyNotImplemented.NotImplemented;
    }

    private static PyLong add0(PyLong a, PyLong b) {
        int sizeA = Math.abs(a.size), sizeB = Math.abs(b.size);
        int carry = 0;

        if (sizeA < sizeB) {
            {
                final PyLong temp = a;
                a = b;
                b = temp;
            }
            {
                final int sizeTemp = sizeA;
                sizeA = sizeB;
                sizeB = sizeTemp;
            }
        }
        final PyLong z = new PyLong(sizeA + 1);
        int i = 0;
        for (i = 0; i < sizeB; i++) {
            carry += a.digits[i] + b.digits[i];
            z.digits[i] = carry & MASK;
            carry >>= SHIFT;
        }
        for (; i < sizeA; i++) {
            carry += a.digits[i];
            z.digits[i] = carry & MASK;
            carry >>= SHIFT;
        }
        z.digits[i] = carry;
        return z.normalize();
    }

    private static PyLong sub0(PyLong a, PyLong b) {
        int sizeA = Math.abs(a.size), sizeB = Math.abs(b.size);
        int sign = 1;
        int borrow = 0;

        if (sizeA < sizeB) {
            sign = -1;
            {
                final PyLong temp = a;
                a = b;
                b = temp;
            }
            {
                final int sizeTemp = sizeA;
                sizeA = sizeB;
                sizeB = sizeTemp;
            }
        } else if (sizeA == sizeB) {
            int i = sizeA;
            while (--i >= 0 && a.digits[i] == b.digits[i]);
            if (i < 0) {
                return fromInt(0);
            }
            if (a.digits[i] < b.digits[i]) {
                sign = -1;
                final PyLong temp = a;
                a = b;
                b = temp;
            }
            sizeA = sizeB = i + 1;
        }
        final PyLong z = new PyLong(sizeA);
        int i = 0;
        for (i = 0; i < sizeB; i++) {
            borrow = a.digits[i] - b.digits[i] - borrow;
            z.digits[i] = borrow & MASK;
            borrow >>= SHIFT;
            borrow &= 1;
        }
        for (; i < sizeA; i++) {
            borrow = a.digits[i] - borrow;
            z.digits[i] = borrow & MASK;
            borrow >>= SHIFT;
            borrow &= 1;
        }
        assert borrow == 0;
        if (sign < 0) {
            z.size = -z.size;
        }
        return z.normalize().maybeSmall();
    }

    private PyLong normalize() {
        int j = Math.abs(size);
        int i = j;
        while (i > 0 && digits[i - 1] == 0) {
            i--;
        }
        if (i != j) {
            size = size < 0 ? -i : i;
        }
        return this;
    }

    private boolean isMediumValue() {
        return size >= 0 && size + 1 < 3;
    }

    private int mediumValue() {
        assert isMediumValue();
        return size * digits[0];
    }

    private PyLong maybeSmall() {
        if (isMediumValue()) {
            int ival = mediumValue();
            if (isSmallInt(ival)) {
                return getSmallInt(ival);
            }
        }
        return this;
    }
}
