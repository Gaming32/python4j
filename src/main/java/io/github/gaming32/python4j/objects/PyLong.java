package io.github.gaming32.python4j.objects;

import java.util.Arrays;

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
            final PyLong val = new PyLong(0);
            if (i > 0) {
                val.digits[0] = i;
                val.size = 1;
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
    private static final int EXP_TABLE_LEN = 1 << (EXP_WINDOW_SIZE - 1);

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
        this.size = size;
        digits = new int[size != 0 ? size : 1];
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
        return 32 - Integer.numberOfLeadingZeros(digit);
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

    private static PyLong getZero() {
        return SMALL_INTS[N_SMALL_NEG_INTS];
    }

    private static PyLong getOne() {
        return SMALL_INTS[N_SMALL_NEG_INTS + 1];
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

    public PyLong mul(PyLong b) {
        if (isMediumValue() && b.isMediumValue()) {
            return fromTwoDigits((long)mediumValue() * (long)b.mediumValue());
        }

        PyLong z = karatsubaMul(this, b);
        if ((size ^ b.size) < 0) {
            z = z.maybeInplaceNegate();
        }
        return z;
    }

    @Override
    public PyObject __mul__(PyObject other) {
        if (other instanceof PyLong) {
            return mul((PyLong)other);
        }
        return PyNotImplemented.NotImplemented;
    }

    private static PyLong karatsubaMul(PyLong a, PyLong b) {
        int aSize = Math.abs(a.size);
        int bSize = Math.abs(b.size);
        PyLong ah = null, al = null, bh = null, bl = null, ret = null;
        int shift; // the number of digits we split off

        /* (ah*X+al)(bh*X+bl) = ah*bh*X*X + (ah*bl + al*bh)*X + al*bl
         * Let k = (ah+al)*(bh+bl) = ah*bl + al*bh  + ah*bh + al*bl
         * Then the original product is
         *     ah*bh*X*X + (k - ah*bh - al*bl)*X + al*bl
         * By picking X to be a power of 2, "*X" is just shifting, and it's
         * been reduced to 3 multiplies on numbers half the size.
         */

        /* We want to split based on the larger number; fiddle so that b
         * is largest.
         */
        if (aSize > bSize) {
            final PyLong t1 = a;
            a = b;
            b = t1;

            final int i = aSize;
            aSize = bSize;
            bSize = i;
        }

        /* Use gradeschool math when either number is too small. */
        int i = a == b ? KARATSUBA_SQUARE_CUTOFF : KARATSUBA_CUTOFF;
        if (aSize <= i) {
            if (aSize == 0) {
                return fromInt(0);
            } else {
                return mulGradescool(a, b);
            }
        }

        if (2 * aSize <= bSize) {
            return karatsubaLopsidedMul(a, b);
        }

        shift = bSize >> 1;
        {
            final PyLong[] split = karatsubaMulSplit(a, shift);
            ah = split[0];
            al = split[1];
        }
        assert ah.size > 0;

        if (a == b) {
            bh = ah;
            bl = al;
        } else {
            final PyLong[] split = karatsubaMulSplit(b, shift);
            bh = split[0];
            bl = split[1];
        }

        /* The plan:
         * 1. Allocate result space (asize + bsize digits:  that's always
         *    enough).
         * 2. Compute ah*bh, and copy into result at 2*shift.
         * 3. Compute al*bl, and copy into result at 0.  Note that this
         *    can't overlap with #2.
         * 4. Subtract al*bl from the result, starting at shift.  This may
         *    underflow (borrow out of the high digit), but we don't care:
         *    we're effectively doing unsigned arithmetic mod
         *    BASE**(sizea + sizeb), and so long as the *final* result fits,
         *    borrows and carries out of the high digit can be ignored.
         * 5. Subtract ah*bh from the result, starting at shift.
         * 6. Compute (ah+al)*(bh+bl), and add it into the result starting
         *    at shift.
         */

        // 1. Allocate result space.
        ret = new PyLong(aSize + bSize);

        // 2. t1 <- ah*bh, and copy into high digits of result.
        PyLong t1 = karatsubaMul(ah, bh);
        assert t1.size >= 0;
        assert 2 * shift + t1.size <= ret.size;
        System.arraycopy(t1.digits, 0, ret.digits, 2 * shift, t1.size);

        // Zero-out the digits higher than the ah*bh copy.
        i = ret.size - 2 * shift - t1.size;
        if (i != 0) {
            Arrays.fill(ret.digits, 2 * shift + t1.size, 2 * shift + t1.size + i, 0);
        }

        // 3. t2 <- al*bl, and copy into the low digits.
        PyLong t2 = karatsubaMul(al, bl);
        assert t2.size >= 0;
        assert t2.size <= 2 * shift;
        System.arraycopy(t2.digits, 0, ret.digits, 0, t2.size);

        // Zero out remaining digits.
        i = 2 * shift - t2.size;
        if (i != 0) {
            Arrays.fill(ret.digits, t2.size, t2.size + i, 0);
        }

        /* 4 & 5. Subtract ah*bh (t1) and al*bl (t2).  We do al*bl first
         * because it's fresher in cache.
         */
        i = ret.size - shift;
        vIsub(ret.digits, shift, i, t2.digits, t2.size);

        vIsub(ret.digits, shift, i, t1.digits, t1.size);

        t1 = add0(ah, al);
        ah = al = null;

        if (a == b) {
            t2 = t1;
        } else {
            t2 = add0(bh, bl);
        }
        bh = bl = null;

        final PyLong t3 = karatsubaMul(t1, t2);
        assert t3.size >= 0;

        // Add t3.
        vIAdd(ret.digits, shift, i, t3.digits, t3.size);

        return ret.normalize();
    }

    private static PyLong karatsubaLopsidedMul(PyLong a, PyLong b) {
        final int aSize = Math.abs(a.size);
        int bSize = Math.abs(b.size);
        PyLong bSlice = null;

        assert aSize > KARATSUBA_CUTOFF;
        assert 2 * aSize <= bSize;

        final PyLong ret = new PyLong(aSize + bSize);

        bSlice = new PyLong(aSize);

        int nbDone = 0;
        while (bSize > 0) {
            final int nbToUse = Math.min(bSize, aSize);

            System.arraycopy(b.digits, nbDone, bSlice.digits, 0, nbToUse);
            bSlice.size = nbToUse;
            final PyLong product = karatsubaMul(a, bSlice);

            vIAdd(ret.digits, nbDone, ret.size - nbDone, product.digits, product.size);

            bSize -= nbToUse;
            nbDone += nbToUse;
        }

        return ret.normalize();
    }

    private static int vIsub(int[] x, int xp, int m, int[] y, int n) {
        int borrow = 0;

        assert m >= n;
        int i;
        for (i = 0; i < n; i++) {
            borrow = x[xp + i] - y[i] - borrow;
            x[xp + i] = borrow & MASK;
            borrow >>= SHIFT;
            borrow &= 1;
        }
        for (; borrow != 0 && i < m; i++) {
            borrow = x[xp + i] - borrow;
            x[xp + i] = borrow & MASK;
            borrow >>= SHIFT;
            borrow &= 1;
        }
        return borrow;
    }

    private static int vIAdd(int[] x, int xp, int m, int[] y, int n) {
        int carry = 0;

        assert m >= n;
        int i;
        for (i = 0; i < n; i++) {
            carry = x[xp + i] + y[i];
            x[xp + i] = carry & MASK;
            carry >>= SHIFT;
            assert (carry & 1) == carry;
        }
        for (; carry != 0 && i < m; i++) {
            carry += x[xp + i];
            x[xp + i] = carry & MASK;
            carry >>= SHIFT;
            assert (carry & 1) == carry;
        }
        return carry;
    }

    private static PyLong mulGradescool(PyLong a, PyLong b) {
        final int sizeA = Math.abs(a.size);
        final int sizeB = Math.abs(b.size);

        final PyLong z = new PyLong(sizeA + sizeB);

        if (a == b) {
            /* Efficient squaring per HAC, Algorithm 14.16:
             * http://www.cacr.math.uwaterloo.ca/hac/about/chap14.pdf
             * Gives slightly less than a 2x speedup when a == b,
             * via exploiting that each entry in the multiplication
             * pyramid appears twice (except for the size_a squares).
             */
            for (int i = 0; i < sizeA; i++) {
                long f = a.digits[i];
                int pz = i << 1;
                int pa = i + 1;

                long carry = z.digits[pz] + f * f;
                z.digits[pz++] = (int)(carry & MASK);
                carry >>= SHIFT;
                assert carry <= MASK;

                /* Now f is added in twice in each column of the
                 * pyramid it appears.  Same as adding f<<1 once.
                 */
                f <<= 1L;
                while (pa < sizeA) {
                    carry += z.digits[pz] + a.digits[pa++] * f;
                    z.digits[pz++] = (int)(carry & MASK);
                    carry >>= SHIFT;
                    assert carry <= MASK << 1;
                }
                if (carry != 0) {
                    /* See comment below. pz points at the highest possible
                     * carry position from the last outer loop iteration, so
                     * *pz is at most 1.
                     */
                    assert z.digits[pz] <= 1;
                    carry += z.digits[pz];
                    z.digits[pz] = (int)(carry & MASK);
                    carry >>= SHIFT;
                    if (carry != 0) {
                        /* If there's still a carry, it must be into a position
                         * that still holds a 0. Where the base
                         ^ B is 1 << PyLong_SHIFT, the last add was of a carry no
                         * more than 2*B - 2 to a stored digit no more than 1.
                         * So the sum was no more than 2*B - 1, so the current
                         * carry no more than floor((2*B - 1)/B) = 1.
                         */
                        assert carry == 1;
                        assert z.digits[pz + 1] == 0;
                        z.digits[pz + 1] = (int)carry;
                    }
                }
            }
        } else { // a is not the same as b -- gradeschool int mult
            for (int i = 0; i < sizeA; i++) {
                long carry = 0;
                final long f = a.digits[i];
                int pz = i;
                int pb = 0;

                while (pb < sizeB) {
                    carry += z.digits[pz] + b.digits[pb++] * f;
                    z.digits[pz++] = (int)(carry & MASK);
                    carry >>= SHIFT;
                    assert carry <= MASK;
                }
                if (carry != 0) {
                    z.digits[pz] += (int)(carry & MASK);
                }
                assert carry >> SHIFT == 0;
            }
        }
        return z.normalize();
    }

    private static PyLong[] karatsubaMulSplit(PyLong n, int size) {
        final int sizeN = Math.abs(n.size);

        final int sizeLo = Math.min(sizeN, size);
        final int sizeHi = sizeN - sizeLo;

        final PyLong hi = new PyLong(sizeHi);
        final PyLong lo = new PyLong(sizeLo);

        System.arraycopy(n.digits, 0, lo.digits, 0, sizeLo);
        System.arraycopy(n.digits, sizeLo, hi.digits, 0, sizeHi);

        return new PyLong[] {hi, lo};
    }

    private PyLong maybeInplaceNegate() {
        if (size == 1 || size == -1) {
            if (isSmallInt(digits[0] * size)) {
                return getSmallInt(digits[0] * -size);
            }
        }
        size = -size;
        return this;
    }

    public PyLong pow(PyLong b) {
        return pow(b, null);
    }

    public PyLong pow(PyLong b, PyLong c) {
        final boolean negativeOutput = false;

        PyLong z = null;
        PyLong a2 = null;

        final PyLong[] table = new PyLong[EXP_TABLE_LEN];
        @SuppressWarnings("unused") int numTableEntries = 0;

        if (b.size < 0 && c == null) {
            throw new UnsupportedOperationException("Not implemented: PyFloat_Type.tp_as_number->nb_power(v, w, x)");
        }

        if (c != null) {
            throw new UnsupportedOperationException("Not implemented: c");
        }

        z = getSmallInt(1);

        // MULT(x, y, r) = (r = x.mul(y))

        int i = b.size;
        int bi = i != 0 ? b.digits[i - 1] : 0;
        if (i <= 1 && bi <= 3) {
            if (bi >= 2) {
                z = mul(this);
                if (bi == 3) {
                    z = z.mul(this);
                }
            } else if (bi == 1) {
                z = mul(z);
            }
        } else if (i <= HUGE_EXP_CUTOFF / SHIFT) {
            assert bi != 0;
            z = this;
            int bit;
            for (bit = 2;; bit <<= 1) {
                if (bit > bi) {
                    assert (bi & bit) == 0;
                    bit >>= 1;
                    assert (bi & bit) != 0;
                    break;
                }
            }
            for (i--, bit >>= 1;;) {
                for (; bit != 0; bit >>= 1) {
                    z = z.mul(z);
                    if ((bi & bit) != 0) {
                        z = z.mul(this);
                    }
                }
                if (--i < 0) break;
                bi = b.digits[i];
                bit = (int)1 << (SHIFT - 1);
            }
        } else {
            table[0] = this;
            numTableEntries = 1;
            a2 = mul(this);
            for (i = 1; i < EXP_TABLE_LEN; i++) {
                table[i] = table[i - 1].mul(a2);
                numTableEntries++;
            }
            a2 = null;

            int pending = 0, blen = 0;
            for (i = b.size - 1; i >= 0; i--) {
                bi = b.digits[i];
                for (int j = SHIFT - 1; j >= 0; j--) {
                    final int bit = (bi >> j) & 1;
                    pending = (pending << 1) | bit;
                    if (pending != 0) {
                        blen++;
                        if (blen == EXP_WINDOW_SIZE) {
                            // region ABSORB_PENDING
                            int ntz = 0;
                            assert pending != 0 && blen != 0;
                            assert (pending >> (blen - 1)) != 0;
                            assert pending >> blen == 0;
                            while ((pending & 1) == 0) {
                                ntz++;
                                pending >>= 1;
                            }
                            assert ntz < blen;
                            blen -= ntz;
                            do {
                                z = z.mul(z);
                            } while (--blen != 0);
                            z = z.mul(table[pending >> 1]);
                            while (ntz-- > 0) {
                                z = z.mul(z);
                            }
                            assert blen == 0;
                            pending = 0;
                            // endregion ABSORB_PENDING
                        }
                    } else {
                        z = z.mul(z);
                    }
                }
            }
            if (pending != 0) {
                // region ABSORB_PENDING
                int ntz = 0;
                assert pending != 0 && blen != 0;
                assert (pending >> (blen - 1)) != 0;
                assert pending >> blen == 0;
                while ((pending & 1) == 0) {
                    ntz++;
                    pending >>= 1;
                }
                assert ntz < blen;
                blen -= ntz;
                do {
                    z = z.mul(z);
                } while (--blen != 0);
                z = z.mul(table[pending >> 1]);
                while (ntz-- > 0) {
                    z = z.mul(z);
                }
                assert blen == 0;
                pending = 0;
                // endregion ABSORB_PENDING
            }
        }

        if (negativeOutput && z.size != 0) {
            z = z.sub(c);
        }

        return z;
    }

    @Override
    public PyObject __pow__(PyObject other) {
        if (other instanceof PyLong) {
            return pow((PyLong)other, null);
        }
        return PyNotImplemented.NotImplemented;
    }

    public PyLong floordiv(PyLong b) {
        if (Math.abs(size) == 1 && Math.abs(b.size) == 1) {
            return fastFloorDiv(this, b);
        }

        final PyLong[] divmod = lDivMod(this, b, true, false);
        return divmod[0];
    }

    @Override
    public PyObject __floordiv__(PyObject other) {
        if (other instanceof PyLong) {
            return floordiv((PyLong)other);
        }
        return PyNotImplemented.NotImplemented;
    }

    public PyLong mod(PyLong b) {
        return lMod(this, b);
    }

    @Override
    public PyObject __mod__(PyObject other) {
        if (other instanceof PyLong) {
            return mod((PyLong)other);
        }
        return PyNotImplemented.NotImplemented;
    }

    public PyLong rem(PyLong b) {
        final int sizeA = Math.abs(size), sizeB = Math.abs(b.size);

        if (sizeB == 0) {
            throw new ArithmeticException("Integer modulo by zero");
        }
        if (sizeA < sizeB || (sizeA == sizeB && digits[sizeA - 1] < b.digits[sizeB - 1])) {
            return this;
        }
        PyLong result;
        if (sizeB == 1) {
            return rem1(this, b.digits[0]);
        } else {
            result = xDivRem(this, b)[1];
        }
        if (size < 0 && result.size != 0) {
            result = result.maybeInplaceNegate();
        }
        return result;
    }

    private static PyLong rem1(PyLong a, int n) {
        final int size = Math.abs(a.size);

        assert n > 0 && n <= MASK;
        return fromInt(inplaceRem1(a.digits, size, n));
    }

    private static int inplaceRem1(int[] pin, int size, int n) {
        long rem = 0;

        assert n > 0 && n <= MASK;
        while (--size >= 0) {
            rem = ((rem << SHIFT) | pin[size]) % n;
        }
        return (int)rem;
    }

    private static PyLong lMod(PyLong v, PyLong w) {
        if (Math.abs(v.size) == 1 && Math.abs(w.size) == 1) {
            return fastMod(v, w);
        }

        PyLong mod = v.rem(w);
        if (
            (mod.size < 0 && w.size > 0) ||
            (mod.size > 0 && w.size < 0)
        ) {
            mod = mod.add(w);
        }
        return mod;
    }

    private static PyLong[] lDivMod(PyLong v, PyLong w, boolean divInterested, boolean modInterested) {
        final PyLong[] result = new PyLong[2];
        if (Math.abs(v.size) == 1 && Math.abs(w.size) == 1) {
            if (divInterested) {
                result[0] = fastFloorDiv(v, w);
            }
            if (modInterested) {
                result[1] = fastMod(v, w);
            }
            return result;
        }
        longDivRem(v, w, result);
        if (
            (result[1].size < 0 && w.size > 0) ||
            (result[1].size > 0 && w.size < 0)
        ) {
            result[1] = result[1].add(w);
            result[0] = result[0].sub(getOne());
        }
        return result;
    }

    private static PyLong fastFloorDiv(PyLong a, PyLong b) {
        final int left = a.digits[0];
        final int right = b.digits[0];

        assert Math.abs(a.size) == 1;
        assert Math.abs(b.size) == 1;

        final int div;
        if (a.size == b.size) {
            div = left / right;
        } else {
            div = -1 - (left - 1) / right;
        }

        return fromInt(div);
    }

    private static PyLong fastMod(PyLong a, PyLong b) {
        final int left = a.digits[0];
        final int right = b.digits[0];

        assert Math.abs(a.size) == 1;
        assert Math.abs(b.size) == 1;

        final int mod;
        if (a.size == b.size) {
            mod = left % right;
        } else {
            mod = right - 1 - (left - 1) % right;
        }

        return fromInt(mod * b.size);
    }

    private static void longDivRem(PyLong a, PyLong b, PyLong[] result) {
        final int sizeA = Math.abs(a.size), sizeB = Math.abs(b.size);
        PyLong z;

        if (sizeB == 0) {
            throw new ArithmeticException("Integer division or modulo by zero");
        }
        if (sizeA < sizeB || (sizeA == sizeB && a.digits[sizeA - 1] < b.digits[sizeB - 1])) {
            result[1] = a;
            result[0] = getZero();
            return;
        }
        if (sizeB == 1) {
            final int[] rem = new int[1];
            z = divRem1(a, b.digits[0], rem);
            result[1] = fromInt(rem[0]);
        } else {
            final PyLong[] divrem = xDivRem(a, b);
            z = divrem[0];
            result[1] = divrem[1].maybeSmall();
        }
        if (a.size < 0 != b.size < 0) {
            z = z.maybeInplaceNegate();
        }
        if (a.size < 0 && result[1].size != 0) {
            result[1] = result[1].maybeInplaceNegate();
        }
        result[0] = z.maybeSmall();
    }

    private static PyLong[] xDivRem(PyLong v1, PyLong w1) {
        int sizeV = Math.abs(v1.size);
        final int sizeW = Math.abs(w1.size);
        assert sizeV >= sizeW && sizeW >= 2;
        final PyLong v = new PyLong(sizeV + 1);
        final PyLong w = new PyLong(sizeW + 1);

        final int d = SHIFT - bitLengthDigit(w1.digits[sizeW - 1]);
        int carry = vLshift(w.digits, 0, w1.digits, sizeW, d);
        assert carry == 0;
        carry = vLshift(v.digits, 0, v1.digits, sizeV, d);
        if (carry != 0 || v.digits[sizeV - 1] >= w.digits[sizeW - 1]) {
            v.digits[sizeV] = carry;
            sizeV++;
        }

        final int k = sizeV - sizeW;
        assert k >= 0;
        final PyLong a = new PyLong(k);
        final int[] v0 = v.digits;
        final int[] w0 = w.digits;
        final int wm1 = w0[sizeW - 1];
        final int wm2 = w0[sizeW - 2];
        for (int vk = k, ak = k; vk-- > 0;) {
            final int vtop = v0[vk + sizeW];
            assert vtop <= wm1;
            final long vv = ((long)vtop << SHIFT) | v0[vk + sizeW - 1];
            int q = (int)(vv / wm1);
            int r = (int)(vv % wm1);
            while ((long)wm2 * q > (((long)r << SHIFT) | v0[vk + sizeW - 2])) {
                q--;
                r += wm1;
                if (r >= BASE) {
                    break;
                }
            }
            assert q <= BASE;

            int zhi = 0;
            for (int i = 0; i < sizeW; i++) {
                final long z = v0[vk + i] + zhi - (long)q * (long)w0[i];
                v0[vk + i] = (int)z & MASK;
                zhi = (int)(z >> SHIFT);
            }

            assert vtop + zhi == -1 || vtop + zhi == 0;
            if (vtop + zhi < 0) {
                carry = 0;
                for (int i = 0; i < sizeW; i++) {
                    carry += v0[vk + i] + w0[i];
                    v0[vk + i] = carry & MASK;
                    carry >>= SHIFT;
                }
                q--;
            }

            assert q < BASE;
            a.digits[--ak] = q;
        }

        carry = vRshift(w0, v0, 0, sizeW, d);
        assert carry == 0;

        return new PyLong[] {a.normalize(), w.normalize()};
    }

    private static PyLong divRem1(PyLong a, int n, int[] prem) {
        final int size = Math.abs(a.size);

        assert n > 0 && n <= MASK;
        final PyLong z = new PyLong(size);
        prem[0] = inplaceDivRem1(z.digits, a.digits, size, n);
        return z.normalize();
    }

    private static int inplaceDivRem1(int[] pout, int[] pin, int size, int n) {
        int remainder = 0;

        assert n > 0 && n <= MASK;
        while (--size >= 0) {
            final long dividend = ((long)remainder << SHIFT) | pin[size];
            final int quotient = (int)(dividend / n);
            remainder = (int)(dividend % n);
            pout[size] = quotient;
        }
        return remainder;
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
