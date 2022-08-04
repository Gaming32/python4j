package io.github.gaming32.python4j;

import java.util.Arrays;

import io.github.gaming32.python4j.nativeapi.DirectIO;

public class IOTest {
    public static void main(String[] args) {
        final byte[] testValue = new byte[] {-64, 53, 6, 28, -96, -10, -111, 14, 8, -94, 110, 103, 71, 33, 107, -40};
        final int[] fds = DirectIO.pipe();
        DirectIO.write(fds[1], testValue);
        System.out.println(Arrays.equals(DirectIO.read(fds[0], testValue.length), testValue));
        DirectIO.close(fds[0]);
        DirectIO.close(fds[1]);

        DirectIO.truncate("test.txt", 10);
    }
}
