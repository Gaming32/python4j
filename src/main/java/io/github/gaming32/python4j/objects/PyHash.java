package io.github.gaming32.python4j.objects;

import java.security.SecureRandom;
import java.util.OptionalLong;
import java.util.function.ToLongFunction;

import io.github.gaming32.python4j.util.UnsafeUtil;
import sun.misc.Unsafe;

public final class PyHash {
    public static final class HashFunctionDefinition {
        private final ToLongFunction<byte[]> hash;
        private final String name;
        private final int hashBits;
        private final int seedBits;

        public HashFunctionDefinition(ToLongFunction<byte[]> hash, String name, int hashBits, int seedBits) {
            this.hash = hash;
            this.name = name;
            this.hashBits = hashBits;
            this.seedBits = seedBits;
        }

        public ToLongFunction<byte[]> getHash() {
            return hash;
        }

        public String getName() {
            return name;
        }

        public int getHashBits() {
            return hashBits;
        }

        public int getSeedBits() {
            return seedBits;
        }
    }

    public static final int MULTIPLIER = 1000003; /* 0xf4243 */
    public static final int BITS = 61;
    public static final long MODULUS = (1L << BITS) - 1;
    public static final int INF = 314159;
    public static final int IMAG = MULTIPLIER;

    private static final OptionalLong HASH_SEED_FROM_PROPERTIES;
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();
    private static long[] hashSecret = null;

    public static final HashFunctionDefinition HASH_FUNCTION = new HashFunctionDefinition(
        PyHash::pysiphash, "siphash13", 64, 128
    );

    static {
        final String hashSeed = System.getProperty("python4j.hashSeed");
        if (hashSeed != null) {
            HASH_SEED_FROM_PROPERTIES = OptionalLong.of(Long.parseLong(hashSeed));
        } else {
            HASH_SEED_FROM_PROPERTIES = OptionalLong.empty();
        }
    }

    private PyHash() {
    }

    public static long pysiphash(byte[] bytes) {
        hashRanomizationInit();
        return siphash13(hashSecret[0], hashSecret[1], bytes);
    }

    public static long siphash13(long k0, long k1, byte[] bytes) {
        long b = bytes.length << 56;

        long v0 = k0 ^ 0x736f6d6570736575L;
        long v1 = k1 ^ 0x646f72616e646f6dL;
        long v2 = k0 ^ 0x6c7967656e657261L;
        long v3 = k1 ^ 0x7465646279746573L;

        long in = Unsafe.ARRAY_BYTE_BASE_OFFSET;
        int sz = bytes.length;
        while (sz >= 8) {
            long mi = UnsafeUtil.getLongUnaligned(bytes, in);
            in += 8;
            sz -= 8;
            v3 ^= mi;
            // SINGLE_ROUND - start
                // HALF_ROUND - start
                    v0 += v1;
                    v2 += v3;
                    v1 = Long.rotateLeft(v1, 13) ^ v0;
                    v3 = Long.rotateLeft(v3, 16) ^ v2;
                    v0 = Long.rotateLeft(v0, 32);
                // HALF_ROUND - end
                // HALF_ROUND - start
                    v2 += v1;
                    v0 += v3;
                    v1 = Long.rotateLeft(v1, 17) ^ v2;
                    v3 = Long.rotateLeft(v3, 21) ^ v0;
                    v2 = Long.rotateLeft(v2, 32);
                // HALF_ROUND - end
            // SINGLE_ROUND - end
            v0 ^= mi;
        }

        long[] t = {0};
        long pt = Unsafe.ARRAY_LONG_BASE_OFFSET;
        switch (sz) {
            case 7: UNSAFE.putByte(t, pt + 6, UNSAFE.getByte(bytes, in + 6));
            case 6: UNSAFE.putByte(t, pt + 5, UNSAFE.getByte(bytes, in + 5));
            case 5: UNSAFE.putByte(t, pt + 4, UNSAFE.getByte(bytes, in + 4));
            case 4: UNSAFE.copyMemory(bytes, pt, t, pt, 4); break;
            case 3: UNSAFE.putByte(t, pt + 2, UNSAFE.getByte(bytes, in + 2));
            case 2: UNSAFE.putByte(t, pt + 1, UNSAFE.getByte(bytes, in + 1));
            case 1: UNSAFE.putByte(t, pt, UNSAFE.getByte(bytes, in));
        }
        b |= t[0];

        v3 ^= b;
        // SINGLE_ROUND - start
            // HALF_ROUND - start
                v0 += v1;
                v2 += v3;
                v1 = Long.rotateLeft(v1, 13) ^ v0;
                v3 = Long.rotateLeft(v3, 16) ^ v2;
                v0 = Long.rotateLeft(v0, 32);
            // HALF_ROUND - end
            // HALF_ROUND - start
                v2 += v1;
                v0 += v3;
                v1 = Long.rotateLeft(v1, 17) ^ v2;
                v3 = Long.rotateLeft(v3, 21) ^ v0;
                v2 = Long.rotateLeft(v2, 32);
            // HALF_ROUND - end
        // SINGLE_ROUND - end
        v0 ^= b;
        v2 ^= 0xff;
        for (int i = 0; i < 3; i++) {
            // SINGLE_ROUND - start
                // HALF_ROUND - start
                    v0 += v1;
                    v2 += v3;
                    v1 = Long.rotateLeft(v1, 13) ^ v0;
                    v3 = Long.rotateLeft(v3, 16) ^ v2;
                    v0 = Long.rotateLeft(v0, 32);
                // HALF_ROUND - end
                // HALF_ROUND - start
                    v2 += v1;
                    v0 += v3;
                    v1 = Long.rotateLeft(v1, 17) ^ v2;
                    v3 = Long.rotateLeft(v3, 21) ^ v0;
                    v2 = Long.rotateLeft(v2, 32);
                // HALF_ROUND - end
            // SINGLE_ROUND - end
        }

        return (v0 ^ v1) ^ (v2 ^ v3);
    }

    private static void hashRanomizationInit() {
        if (hashSecret != null) return;
        if (HASH_SEED_FROM_PROPERTIES.isPresent() && HASH_SEED_FROM_PROPERTIES.getAsLong() == 0L) {
            hashSecret = new long[] {0L, 0L};
            return;
        }
        final SecureRandom random = new SecureRandom();
        if (HASH_SEED_FROM_PROPERTIES.isPresent()) {
            random.setSeed(HASH_SEED_FROM_PROPERTIES.getAsLong());
        }
        hashSecret = new long[] {random.nextLong(), random.nextLong()};
    }
}
