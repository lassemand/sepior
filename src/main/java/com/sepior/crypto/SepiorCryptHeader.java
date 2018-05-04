/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SepiorCryptHeader {
    private int cryptApiVersion;
    private int fileKeyVersion;
    private int masterKeyVersion;
    private final String fileUid;
    private final String sepiorCryptId;
    private static final String FILE_UID_ENCODING = "UTF-8";
    private static final String CRYPT_ID_ENCODING = "US-ASCII";
    private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;

    public static SepiorCryptHeader parse(Path encryptedFile) throws IOException {
        byte[] headerBytes = new byte[SepiorCryptConstants.HEADER_SIZE];
        try (InputStream is = Files.newInputStream(encryptedFile)) {
            int totalRead = 0;
            int read;
            while (totalRead < headerBytes.length
                    && (read = is.read(headerBytes, totalRead, headerBytes.length - totalRead)) != -1) {
                totalRead += read;
            }
            if (totalRead != headerBytes.length) {
                throw new IOException("Not a valid header. Only " + totalRead + " bytes found.");
            }
            return parse(headerBytes);
        }
    }

    public static SepiorCryptHeader parse(byte[] bytes) throws IOException {
        if (bytes == null) {
            throw new IOException("Cannot parse null.");
        }
        if (bytes.length != SepiorCryptConstants.HEADER_SIZE) {
            throw new IOException("Invalid size for header: " + bytes.length + ". Excepted "
                    + SepiorCryptConstants.HEADER_SIZE);
        }
        ByteBuffer b = ByteBuffer.wrap(bytes);
        b.order(BYTEORDER);
        int cryptApiVersion = b.getInt();
        int fileKeyVersion = b.getInt();
        int masterKeyVersion = b.getInt();
        int fileUidSize = b.getInt();
        if (fileUidSize < 0 || fileUidSize > SepiorCryptConstants.SEPIOR_FILE_UID_MAX_SIZE) {
            throw new IOException("Invalid file uid length.");
        }
        byte[] fileUidEncoded = new byte[fileUidSize];
        b.get(fileUidEncoded);
        String fileUid = new String(fileUidEncoded, FILE_UID_ENCODING);
        b.get(new byte[SepiorCryptConstants.SEPIOR_FILE_UID_MAX_SIZE - fileUidEncoded.length]);
        byte[] cryptIdEncoded = new byte[SepiorCryptConstants.SEPIOR_CRYPT_ID_SIZE];
        b.get(cryptIdEncoded);
        String cryptId = new String(cryptIdEncoded, CRYPT_ID_ENCODING);
        if (!SepiorCryptConstants.SEPIOR_CRYPT_ID.equals(cryptId)) {
            throw new IOException("Unexpected Sepior crypt id: " + cryptId);
        }
        b.get(new byte[SepiorCryptConstants.SEPIOR_HEADER_PADDING]);

        return new SepiorCryptHeader(cryptApiVersion, masterKeyVersion, fileKeyVersion, fileUid, cryptId);
    }

    public static SepiorCryptHeader parse(InputStream stream) throws IOException {
        SepiorCryptHeader parsedHeader = null;
        byte[] headerBytes = new byte[SepiorCryptConstants.HEADER_SIZE];
        stream.read(headerBytes);
        parsedHeader = SepiorCryptHeader.parse(headerBytes);
        return parsedHeader;
    }

    public SepiorCryptHeader(int cryptApiVersion, int masterKeyVersion, int fileKeyVersion, String fileUid,
            String cryptId) {
        this.cryptApiVersion = cryptApiVersion;
        this.fileKeyVersion = fileKeyVersion;
        this.masterKeyVersion = masterKeyVersion;
        this.fileUid = fileUid;
        this.sepiorCryptId = cryptId;
    }

    public SepiorCryptHeader(String fileUid, int masterKeyVersion, int fileKeyVersion) {
        this.cryptApiVersion = SepiorCryptConstants.SEPIOR_CRYPT_API_VERSION;
        this.fileKeyVersion = fileKeyVersion;
        this.masterKeyVersion = masterKeyVersion;
        this.sepiorCryptId = SepiorCryptConstants.SEPIOR_CRYPT_ID;
        this.fileUid = fileUid;
    }

    public byte[] serialize() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(SepiorCryptConstants.HEADER_SIZE);
        buffer.order(BYTEORDER);
        buffer.putInt(cryptApiVersion);
        buffer.putInt(fileKeyVersion);
        buffer.putInt(masterKeyVersion);
        byte[] fileUidEncoded = fileUid.getBytes(FILE_UID_ENCODING);
        if (fileUidEncoded.length > SepiorCryptConstants.SEPIOR_FILE_UID_MAX_SIZE) {
            throw new IOException("File UID was too long: " + fileUidEncoded.length);
        }
        buffer.putInt(fileUidEncoded.length); // fileUID size;
        buffer.put(fileUidEncoded);
        buffer.put(new byte[SepiorCryptConstants.SEPIOR_FILE_UID_MAX_SIZE - fileUidEncoded.length]);
        byte[] cryptIdEncoded = sepiorCryptId.getBytes(CRYPT_ID_ENCODING);
        if (cryptIdEncoded.length != SepiorCryptConstants.SEPIOR_CRYPT_ID_SIZE) {
            throw new IOException("Sepior Crypt Id was of an unexpected size: " + cryptIdEncoded.length);
        }
        buffer.put(cryptIdEncoded);
        buffer.put(new byte[SepiorCryptConstants.SEPIOR_HEADER_PADDING]);
        return buffer.array();
    }

    public int getCryptApiVersion() {
        return cryptApiVersion;
    }

    public int getFileKeyVersion() {
        return fileKeyVersion;
    }

    public int getMasterKeyVersion() {
        return masterKeyVersion;
    }

    public String getFileUid() {
        return fileUid;
    }

    public String getSepiorCryptId() {
        return sepiorCryptId;
    }

}
