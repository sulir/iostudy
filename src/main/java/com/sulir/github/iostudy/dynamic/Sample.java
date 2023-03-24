package com.sulir.github.iostudy.dynamic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Sample {
    public static void main(String[] args) {
        computeSquareRoot();
    }

    public static void computeSquareRoot() {
        long start = System.currentTimeMillis();
        try (OutputStream out = OutputStream.nullOutputStream()) {
            for (int i = 0; i < 10_000; i++) {
                out.write(ByteBuffer.allocate(8).putDouble(Math.sqrt(i)).array());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Time: " + (System.currentTimeMillis() - start));
    }
}
