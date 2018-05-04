/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.performance;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class TestUtils {

    public static Path createRandomTempFile(long fileSize) throws IOException {
        Random random = new Random();
        int maxBufferSize = 1024;
        byte[] buffer = new byte[maxBufferSize];
        Path plaintextPath = Files.createTempFile("random" + fileSize, "plain");
        plaintextPath.toFile().deleteOnExit();
        OutputStream fileOutputStream = Files.newOutputStream(plaintextPath);
        int written = 0;
        while (written < fileSize) {
            if (fileSize-written < buffer.length) {
                buffer = new byte[(int) (fileSize-written)];
            }
            random.nextBytes(buffer);
            fileOutputStream.write(buffer);
            written += buffer.length;
        }
        fileOutputStream.close();

        return plaintextPath;
    }


    /**
     * Get the 'official' tests key shares. These are the key shares that are used to
     * encrypt data in our test vector directory.
     * 
     */
    public static SecretKey[] getTestKeys() {
        byte[] encodedKey1 = DatatypeConverter
                .parseHexBinary("31323334353637383132333435363738");
        byte[] encodedKey2 = DatatypeConverter
                .parseHexBinary("32333435363738393233343536373839");
        byte[] encodedKey3 = DatatypeConverter
                .parseHexBinary("33343536373839303334353637383930");

        SecretKey key1 = new SecretKeySpec(encodedKey1, 0, encodedKey1.length,
                "AES");
        SecretKey key2 = new SecretKeySpec(encodedKey2, 0, encodedKey1.length,
                "AES");
        SecretKey key3 = new SecretKeySpec(encodedKey3, 0, encodedKey1.length,
                "AES");
        return new SecretKey[] { key1, key2, key3 };
    }


}
