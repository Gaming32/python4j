package io.github.gaming32.python4j;

import static io.github.gaming32.python4j.UnicodeTypeDB.*;

public final class UnicodeType {
    private static final int ALPHA_MASK = 0x01;
    private static final int DECIMAL_MASK = 0x02;
    private static final int DIGIT_MASK = 0x04;
    private static final int LOWER_MASK = 0x08;
    private static final int TITLE_MASK = 0x40;
    private static final int UPPER_MASK = 0x80;
    private static final int XID_START_MASK = 0x100;
    private static final int XID_CONTINUE_MASK = 0x200;
    private static final int PRINTABLE_MASK = 0x400;
    private static final int NUMERIC_MASK = 0x800;
    private static final int CASE_IGNORABLE_MASK = 0x1000;
    private static final int CASED_MASK = 0x2000;
    private static final int EXTENDED_CASE_MASK = 0x4000;

    private UnicodeType() {
    }

    static int[] getTypeRecord(int code) {
        int index;

        if (code >= 0x110000) {
            index = 0;
        } else {
            index = index1[code >> SHIFT];
            index = index2[(index << SHIFT) + (code & ((1 << SHIFT) - 1))];
        }

        return TypeRecords[index];
    }

    public static int toTitleCase(int ch) {
        final int[] ctype = getTypeRecord(ch);

        if ((ctype[flags] & EXTENDED_CASE_MASK) != 0) {
            return ExtendedCase[ctype[title] & 0xFFFF];
        }
        return ch + ctype[title];
    }

    public static boolean isTitleCase(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & TITLE_MASK) != 0;
    }

    public static boolean isXidStart(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & XID_START_MASK) != 0;
    }

    public static boolean isXidContinue(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & XID_CONTINUE_MASK) != 0;
    }

    public static int toDecimalDigit(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & DECIMAL_MASK) != 0 ? ctype[decimal] : -1;
    }

    public static boolean isDecimalDigit(int ch) {
        if (toDecimalDigit(ch) < 0) {
            return false;
        }
        return true;
    }

    public static int toDigit(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & DIGIT_MASK) != 0 ? ctype[digit] : -1;
    }

    public static boolean isDigit(int ch) {
        if (toDigit(ch) < 0) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & NUMERIC_MASK) != 0;
    }

    public static boolean isPrintable(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & PRINTABLE_MASK) != 0;
    }

    public static boolean isLowercase(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & LOWER_MASK) != 0;
    }

    public static boolean isUppercase(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & UPPER_MASK) != 0;
    }

    public static int toUppercase(int ch) {
        final int[] ctype = getTypeRecord(ch);

        if ((ctype[flags] & EXTENDED_CASE_MASK) != 0) {
            return ExtendedCase[ctype[upper] & 0xFFFF];
        }
        return ch + ctype[upper];
    }

    public static int toLowercase(int ch) {
        final int[] ctype = getTypeRecord(ch);

        if ((ctype[flags] & EXTENDED_CASE_MASK) != 0) {
            return ExtendedCase[ctype[lower] & 0xFFFF];
        }
        return ch + ctype[lower];
    }

    public static int toLowerFull(int ch, int[] res) {
        final int[] ctype = getTypeRecord(ch);

        if ((ctype[flags] & EXTENDED_CASE_MASK) != 0) {
            final int index = ctype[lower] & 0xFFFF;
            final int n = ctype[lower] >> 24;
            for (int i = 0; i < n; i++) {
                res[i] = ExtendedCase[index + i];
            }
            return n;
        }
        res[0] = ch + ctype[lower];
        return 1;
    }

    public static int toTitleFull(int ch, int[] res) {
        final int[] ctype = getTypeRecord(ch);

        if ((ctype[flags] & EXTENDED_CASE_MASK) != 0) {
            final int index = ctype[title] & 0xFFFF;
            final int n = ctype[title] >> 24;
            for (int i = 0; i < n; i++) {
                res[i] = ExtendedCase[index + i];
            }
            return n;
        }
        res[0] = ch + ctype[title];
        return 1;
    }

    public static int toUpperFull(int ch, int[] res) {
        final int[] ctype = getTypeRecord(ch);

        if ((ctype[flags] & EXTENDED_CASE_MASK) != 0) {
            final int index = ctype[upper] & 0xFFFF;
            final int n = ctype[upper] >> 24;
            for (int i = 0; i < n; i++) {
                res[i] = ExtendedCase[index + i];
            }
            return n;
        }
        res[0] = ch + ctype[upper];
        return 1;
    }

    public static int toFoldedFull(int ch, int[] res) {
        final int[] ctype = getTypeRecord(ch);

        if ((ctype[flags] & EXTENDED_CASE_MASK) != 0 && ((ctype[lower] >> 20) & 7) != 0) {
            final int index = (ctype[lower] & 0xFFFF) + (ctype[lower] >> 24);
            final int n = (ctype[lower] >> 20) & 7;
            for (int i = 0; i < n; i++) {
                res[i] = ExtendedCase[index + i];
            }
            return n;
        }
        return toLowerFull(ch, res);
    }

    public static boolean isCased(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & CASED_MASK) != 0;
    }

    public static boolean isCaseIgnorable(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & CASE_IGNORABLE_MASK) != 0;
    }

    public static boolean isAlpha(int ch) {
        final int[] ctype = getTypeRecord(ch);

        return (ctype[flags] & ALPHA_MASK) != 0;
    }
}
