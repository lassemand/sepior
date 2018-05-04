/* Copyright (c) 2013-2015 Sepior Aps, all rights reserved. */
package com.sepior.performance;

import com.sepior.crypto.EncryptingOutputStream;
import com.sepior.streams.StreamUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class EncryptionPerformanceTest {

    public static void main(String[] args) throws Exception {
        testPerformanceEncrypt(10); // For warm-up.
        testPerformanceEncrypt(1024);
        testPerformanceEncrypt(1024 * 1024);
        testPerformanceEncrypt(1024 * 1024 * 1024);
    }

    public static void testPerformanceEncrypt(long length) throws Exception {
        System.out.println("Encrypting with EncryptingOutputStream.");
        System.out.println("Creating random file of length " + length + "...");
        Path plaintextPath = TestUtils.createRandomTempFile(length);
        System.out.println("Encrypting file...");
        Path encryptionPath = Files.createTempFile("random" + length, "enc");
        long before = System.currentTimeMillis();
        EncryptingOutputStream encryptingOutputStream = new EncryptingOutputStream(
                Files.newOutputStream(encryptionPath), "fileUid", 0, 0, TestUtils.getTestKeys());

        // mv: This seems to be the optimal buffer size on my machine.
        long copied = StreamUtils.copyStream(Files.newInputStream(plaintextPath), encryptingOutputStream, 16 * 1024);
        if (copied != length) {
            throw new IllegalStateException("Encrypted data did not have correct length: " + copied);
        }

        long millis = System.currentTimeMillis() - before;
        System.out.println("Done. Took " + millis + " ms; " + (1000 * length / (double) millis / 1024.0 / 1024.0) + " Mb/s");
        System.out.println("-------------------");
        Files.delete(plaintextPath);
        Files.delete(encryptionPath);
    }

}
