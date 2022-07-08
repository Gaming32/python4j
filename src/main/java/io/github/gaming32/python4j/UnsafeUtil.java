package io.github.gaming32.python4j;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

import sun.misc.Unsafe;

public final class UnsafeUtil {
    private static final Unsafe UNSAFE;

    static {
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe)unsafeField.get(null);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * Fetches a value at some byte offset into a given Java object.
     * More specifically, fetches a value within the given object
     * <code>o</code> at the given offset, or (if <code>o</code> is
     * null) from the memory address whose numerical value is the
     * given offset.  <p>
     *
     * The specification of this method is the same as {@link
     * #getLong(Object, long)} except that the offset does not need to
     * have been obtained from {@link #objectFieldOffset} on the
     * {@link java.lang.reflect.Field} of some Java field.  The value
     * in memory is raw data, and need not correspond to any Java
     * variable.  Unless <code>o</code> is null, the value accessed
     * must be entirely within the allocated object.  The endianness
     * of the value in memory is the endianness of the native platform.
     *
     * <p> The read will be atomic with respect to the largest power
     * of two that divides the GCD of the offset and the storage size.
     * For example, getLongUnaligned will make atomic reads of 2-, 4-,
     * or 8-byte storage units if the offset is zero mod 2, 4, or 8,
     * respectively.  There are no other guarantees of atomicity.
     * <p>
     * 8-byte atomicity is only guaranteed on platforms on which
     * support atomic accesses to longs.
     *
     * @param o Java heap object in which the value resides, if any, else
     *        null
     * @param offset The offset in bytes from the start of the object
     * @return the value fetched from the indicated object
     * @throws RuntimeException No defined exceptions are thrown, not even
     *         {@link NullPointerException}
     * @since 9
     */
    public static long getLongUnaligned(Object o, long offset) {
        if ((offset & 7) == 0) {
            return UNSAFE.getLong(o, offset);
        } else if ((offset & 3) == 0) {
            return makeLong(UNSAFE.getInt(o, offset),
                            UNSAFE.getInt(o, offset + 4));
        } else if ((offset & 1) == 0) {
            return makeLong(UNSAFE.getShort(o, offset),
                            UNSAFE.getShort(o, offset + 2),
                            UNSAFE.getShort(o, offset + 4),
                            UNSAFE.getShort(o, offset + 6));
        } else {
            return makeLong(UNSAFE.getByte(o, offset),
                            UNSAFE.getByte(o, offset + 1),
                            UNSAFE.getByte(o, offset + 2),
                            UNSAFE.getByte(o, offset + 3),
                            UNSAFE.getByte(o, offset + 4),
                            UNSAFE.getByte(o, offset + 5),
                            UNSAFE.getByte(o, offset + 6),
                            UNSAFE.getByte(o, offset + 7));
        }
    }

    private static long makeLong(int i0, int i1) {
        return (toUnsignedLong(i0) << pickPos(32, 0))
             | (toUnsignedLong(i1) << pickPos(32, 32));
    }

    private static long makeLong(short i0, short i1, short i2, short i3) {
        return ((toUnsignedLong(i0) << pickPos(48, 0))
              | (toUnsignedLong(i1) << pickPos(48, 16))
              | (toUnsignedLong(i2) << pickPos(48, 32))
              | (toUnsignedLong(i3) << pickPos(48, 48)));
    }

    private static long makeLong(byte i0, byte i1, byte i2, byte i3, byte i4, byte i5, byte i6, byte i7) {
        return ((toUnsignedLong(i0) << pickPos(56, 0))
              | (toUnsignedLong(i1) << pickPos(56, 8))
              | (toUnsignedLong(i2) << pickPos(56, 16))
              | (toUnsignedLong(i3) << pickPos(56, 24))
              | (toUnsignedLong(i4) << pickPos(56, 32))
              | (toUnsignedLong(i5) << pickPos(56, 40))
              | (toUnsignedLong(i6) << pickPos(56, 48))
              | (toUnsignedLong(i7) << pickPos(56, 56)));
    }

    private static long toUnsignedLong(int n)   { return n & 0xffffffffl; }

    private static final boolean BE = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private static int pickPos(int top, int pos) { return BE ? top - pos : pos; }
}
