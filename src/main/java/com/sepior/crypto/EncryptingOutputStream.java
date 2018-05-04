/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.crypto;

import com.sepior.streams.ConcurrentOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class EncryptingOutputStream extends OutputStream {
    private final OutputStream stream;
    private final SecretKey[] keys;
    private final String fileUid;
    private final int masterKeyVersion;
    private final int resourceKeyVersion;

    private OutputStream chainedCipherOutputStream;

    public EncryptingOutputStream(OutputStream stream, String documentId, int masterKeyVersion, int resourceKeyVersion, SecretKey... keys) {
        this.stream = stream;
        this.keys = keys;
        this.fileUid = documentId;
        this.masterKeyVersion = masterKeyVersion;
        this.resourceKeyVersion = resourceKeyVersion;
    }

    public EncryptingOutputStream(OutputStream stream, String documentId, int masterKeyVersion, int resourceKeyVersion, byte[]... keys) {
        this(stream, documentId, masterKeyVersion, resourceKeyVersion, fromByteArrays(keys));
    }
    
    //To reuse constructor code
    static SecretKey[] fromByteArrays(byte[]... keys) {
        SecretKey[] res = new SecretKey[keys.length];
        for (int i = 0; i < keys.length; i++) {
            res[i] = new SecretKeySpec(keys[i], 0, keys[i].length, "AES");
        }
        return res;
    }

    
    @Override
    public void flush() throws IOException {
        if (chainedCipherOutputStream != null) {
            chainedCipherOutputStream.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        if (chainedCipherOutputStream != null) {
            chainedCipherOutputStream.close();
        }
    }

    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[] { (byte) b };
        write(bytes);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (chainedCipherOutputStream == null) {
            writeHeaderAndIv();
        }
        chainedCipherOutputStream.write(b, off, len);
    }

    private byte[] generateHeader() throws IOException {
        SepiorCryptHeader header = new SepiorCryptHeader(fileUid, masterKeyVersion, resourceKeyVersion);
        return header.serialize();
    }

    private void writeHeaderAndIv() throws IOException {
        try {
            stream.write(generateHeader());
            final byte[] nonce = new byte[SepiorCryptConstants.NONCE_SIZE];
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(nonce);
            stream.write(nonce);
            for (SecretKey key : keys) {
                byte[] ivBytes = new byte[16];
                System.arraycopy(nonce, 0, ivBytes, 0, SepiorCryptConstants.NONCE_SIZE);
                IvParameterSpec iv = new IvParameterSpec(ivBytes);
                Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
                if (this.chainedCipherOutputStream == null) {
                    chainedCipherOutputStream = new ConcurrentOutputStream(new CipherOutputStream(stream, cipher));
                }
                else {
                    chainedCipherOutputStream = new ConcurrentOutputStream(new CipherOutputStream(chainedCipherOutputStream,
                            cipher));
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IOException(e);
        }

    }

}
