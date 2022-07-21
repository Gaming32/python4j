package io.github.gaming32.python4j.objects;

public class PyLong extends PyVarObject {
    static final int SHIFT = 30;
    private static final int BASE = 1 << SHIFT;
    private static final int MASK = BASE - 1;

    private static final int N_SMALL_NEG_INTS = 5;
    private static final int N_SMALL_POS_INTS = 257;
    private static final PyLong[] SMALL_INTS = new PyLong[N_SMALL_NEG_INTS + N_SMALL_POS_INTS];

    static {
        for (int i = -N_SMALL_NEG_INTS; i < N_SMALL_POS_INTS; i++) {
            PyLong val = new PyLong(1);
            val.digits[0] = i;
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
        digits = new int[this.size = size];
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
    public String __repr__() {
        // TODO: reimplement to support values > Integer.MAX_VALUE
        return String.valueOf(digits[0]);
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

    private static boolean isSmallInt(int ival) {
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
                        res = -1;
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
