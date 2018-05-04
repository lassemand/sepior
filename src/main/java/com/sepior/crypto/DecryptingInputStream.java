/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.crypto;

import com.sepior.streams.StreamUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class DecryptingInputStream extends InputStream {
    private final InputStream stream;
    private final SecretKey[] keys;
    private SepiorCryptHeader parsedHeader = null;

    private InputStream chainedCipherInputStream;

    public DecryptingInputStream(InputStream stream, SecretKey... keys) {
        this.stream = stream;
        this.keys = keys;
    }

    public DecryptingInputStream(InputStream stream, byte[]... keys) {
        this(stream, EncryptingOutputStream.fromByteArrays(keys));
    }

    /**
     * This method allows for setting the header before reading from the stream.
     * If the header has been set, it is assumed that the next bytes from the
     * underlying stream will be actual encrypted data.
     */
    public void setHeader(SepiorCryptHeader header) {
        parsedHeader = header;
    }

    @Override
    public void close() throws IOException {
        stream.close();
        if(chainedCipherInputStream == null){
            return;
        }
        chainedCipherInputStream.close();
        chainedCipherInputStream = null;
    }

    @Override
    public int read() throws IOException {
        return StreamUtils.read(this);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (chainedCipherInputStream == null) {
            initialize();
        }
        return chainedCipherInputStream.read(b, off, len);
    }

    void initialize() throws IOException {
        if (parsedHeader == null) {
            byte[] header = new byte[SepiorCryptConstants.HEADER_SIZE];
            readRequiredField(header);
            parsedHeader = SepiorCryptHeader.parse(header);
        }
        if (parsedHeader.getCryptApiVersion() > SepiorCryptConstants.SEPIOR_CRYPT_API_VERSION) {
            throw new IOException("The data was encrypted with an unknown (newer) version of the Sepior crypt api: "
                    + parsedHeader.getCryptApiVersion());
        }
        if (parsedHeader.getCryptApiVersion() == 0) {
            throw new IOException("The data was encrypted with a version of the Sepior crypt api that is not longer supported.");
        }
        byte[] nonce = new byte[SepiorCryptConstants.NONCE_SIZE];
        readRequiredField(nonce);

        for (SecretKey key : keys) {
            byte[] ivBytes = new byte[16];
            System.arraycopy(nonce, 0, ivBytes, 0, SepiorCryptConstants.NONCE_SIZE);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            try {
                Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
                if (this.chainedCipherInputStream == null) {
//                    chainedCipherInputStream = new ConcurrentInputStream(new CipherInputStream(stream, cipher));
                     chainedCipherInputStream = new CipherInputStream(stream,
                     cipher);
                }
                else {
//                    chainedCipherInputStream = new ConcurrentInputStream(new CipherInputStream(chainedCipherInputStream, cipher));
                     chainedCipherInputStream = new
                     CipherInputStream(chainedCipherInputStream, cipher);
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException
                    | InvalidKeyException | InvalidAlgorithmParameterException e) {
                throw new IOException(e);
            }
        }
    }

    private void readRequiredField(byte[] b) throws IOException {
        int bytesRead = 0;
        while (bytesRead < b.length) {
            int read = stream.read(b, bytesRead, b.length - bytesRead);
            if (read == -1) {
                throw new IOException("Not a valid Sepior encryption.");
            }
            bytesRead += read;
        }
    }

}
