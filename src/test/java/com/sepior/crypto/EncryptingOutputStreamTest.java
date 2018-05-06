/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.crypto;

import com.sepior.performance.TestUtils;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class EncryptingOutputStreamTest {

    @Test
    public void testEncryptDecryptCorrespondence() throws IOException {
        int fileSize = 1024 * 1024 * 10;

        Path plaintextPath = TestUtils.createRandomTempFile(fileSize);

        Path encryptionPath = Files.createTempFile("random" + fileSize, "enc");
        Path decryptedEncryptedPath = Files.createTempFile("random" + fileSize, "dec");
        SecretKey[] keys = TestUtils.getTestKeys();
        EncryptingOutputStream encryptingOutputStream = new EncryptingOutputStream(Files.newOutputStream(encryptionPath),
                "fileUid", 0, 0, keys);

        InputStream fileInputStream = Files.newInputStream(plaintextPath);
        int read;
        byte[] buffer = new byte[1024];
        while ((read = fileInputStream.read(buffer)) != -1) {
            encryptingOutputStream.write(buffer, 0, read);
        }
        encryptingOutputStream.close();
        fileInputStream.close();
        InputStream encryptedInputStream = Files.newInputStream(encryptionPath);
        InputStream inputStream = new DecryptingInputStream(encryptedInputStream, keys);
        OutputStream decryptedEncryptedOutputStream = Files.newOutputStream(decryptedEncryptedPath);

        while ((read = inputStream.read(buffer)) != -1) {
            decryptedEncryptedOutputStream.write(buffer, 0, read);
        }

        inputStream.close();
        decryptedEncryptedOutputStream.close();

        byte[] b1 = Files.readAllBytes(decryptedEncryptedPath), b2 = Files.readAllBytes(plaintextPath);

        assertEquals(b1.length, b2.length);
        for(int i = 0; i < b1.length; i++){
            assertEquals("Failed at position "+i, b1[i], b2[i]);
        }
        Files.delete(plaintextPath);
        Files.delete(encryptionPath);
        Files.delete(decryptedEncryptedPath);
    }
    
    @Test
    public void testEncryptionSize() throws IOException {
        checkEncryptionSize(0);
        checkEncryptionSize(1);
        checkEncryptionSize(2);
        checkEncryptionSize(3);
        checkEncryptionSize(4);
        checkEncryptionSize(5);
        checkEncryptionSize(6);
        checkEncryptionSize(7);
        checkEncryptionSize(8);
        checkEncryptionSize(9);
        checkEncryptionSize(10);
        checkEncryptionSize(11);
        checkEncryptionSize(12);
        checkEncryptionSize(13);
        checkEncryptionSize(14);
        checkEncryptionSize(16);
        checkEncryptionSize(17);

        checkEncryptionSize(517);
        
        checkEncryptionSize(1024);
        checkEncryptionSize(1024+1);
        checkEncryptionSize(1024+2);
        checkEncryptionSize(1024+3);
        checkEncryptionSize(1024+23);
        
        checkEncryptionSize(1024*1024);
        checkEncryptionSize(1024*1024+1);
        checkEncryptionSize(1024*1024+2);
        checkEncryptionSize(1024*1024+3);
        checkEncryptionSize(1024*1024+23);
    }
    
    private void checkEncryptionSize(int fileSize) throws IOException {
        Path plaintextPath = TestUtils.createRandomTempFile(fileSize);
        assertEquals(fileSize, Files.size(plaintextPath));
        Path encryptionPath = Files.createTempFile("random"+fileSize,  "enc");
        SecretKey[] keys = TestUtils.getTestKeys();
        EncryptingOutputStream encryptingOutputStream = new EncryptingOutputStream(Files.newOutputStream(encryptionPath),
                "fileUid", 0, 0, keys);
        InputStream fileInputStream = Files.newInputStream(plaintextPath);
        if (fileSize == 0) {
            encryptingOutputStream.write(new byte[0], 0, 0);
        } else {
            int read;
            byte[] buffer = new byte[1024];
            while ((read = fileInputStream.read(buffer)) != -1) {
                encryptingOutputStream.write(buffer, 0, read);
            }
        }
        encryptingOutputStream.close();
        fileInputStream.close();
        System.out.println("This should be called in the end");
        assertEquals(fileSize + SepiorCryptConstants.HEADER_SIZE+SepiorCryptConstants.NONCE_SIZE, Files.size(encryptionPath));
        
        Files.delete(plaintextPath);
        Files.delete(encryptionPath);
    }

}
