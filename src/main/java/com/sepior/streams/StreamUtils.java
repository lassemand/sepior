/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.streams;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Utilities for working with and testing streams.
 *
 * @author mv
 *
 */
public class StreamUtils {
    /**
     * Handy for test purposes.
     */
    public static int readFromStreamAndClose(InputStream is, byte[] buffer) throws IOException{
        int res = is.read(buffer);
        is.close();
        return res;
    }

    /**
     * Copy an InputStream to an OutputStream.
     * Both streams will be closed after the operation.
     *
     * @param src
     *            The source InputStream.
     * @param dest
     *            The destination OutputStream.
     * @param bufferSize The size of the buffer used for copying.
     * @return The number of bytes copied
     * @throws IOException
     */
    public static long copyStream(InputStream src, OutputStream dest, int bufferSize) throws IOException {
        long res = 0;
        try {
            byte[] buffer = new byte[bufferSize];
            int read;
            while ((read = src.read(buffer)) != -1) {
                dest.write(buffer, 0, read);
                res += read;

            }
            return res;
        } finally {
            src.close();
            dest.close();
        }

    }

    public static long copyStream(InputStream src, OutputStream dest) throws IOException {
        final int BUFFER_SIZE = 16 * 1024;
        return copyStream(src, dest, BUFFER_SIZE);
    }

    /**
     * Returns all data from an InputStream as a byte array.
     * The InputStream will be closed after this operation.
     *
     * @param is
     *            The InputStream to read data from.
     * @return The data as a byte array.
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream targetOs = new ByteArrayOutputStream();
        copyStream(is, targetOs);
        return targetOs.toByteArray();
    }

    /**
     * Aggressively reads bytes from an InputStream into a buffer.
     * After the operation the buffer will be completely full unless EOF has been reached.
     * If an InputStream is slow or returns data in smaller chunks, a call to its read(byte[]) method
     * may not fill the buffer. This operation will block until that enough data is available (or EOF
     * has been reached).
     *
     * @param stream
     *            The InputStream to read from.
     * @param b
     *            The buffer to be filled.
     * @throws IOException
     */
    public static int fillBytes(InputStream stream, byte[] b) throws IOException {
        return fillBytes(stream, b, 0, b.length);
    }

    public static int fillBytes(InputStream stream, byte[] b, int off, int len) throws IOException {
        int totalRead = 0;
        while (totalRead < len) {
            int read = stream.read(b, off + totalRead, len - totalRead);
            if (read == -1) {
                break;
            }
            totalRead += read;
        }
        if (totalRead == 0) {
            return -1;
        }
        return totalRead;
    }

    /**
     * Checks whether two InputStreams are equal i.e. returns the same bytes.
     * Both streams will be closed after this operation.
     *
     * @throws IOException
     */
    public static boolean isEqualAndClose(InputStream is1, InputStream is2) throws IOException {
        try {
            byte[] buffer1 = new byte[512];
            byte[] buffer2 = new byte[buffer1.length];
            int read1;
            int read2;
            do {
                read1 = fillBytes(is1, buffer1);
                read2 = fillBytes(is2, buffer2);
                if (read1 != read2) {
                    return false;
                }
                if (!Arrays.equals(buffer1, buffer2)) {
                    return false;
                }
            } while (read1 != -1);
            return true;
        } finally {
            is1.close();
            is2.close();
        }
    }

    /**
     * 'Røvballe' implementation of InputStream.read().
     * Handy for allmost all concrete implementations of InputStream:
     *
     * @param inputStream
     *            The InputStream to read from.
     * @return The byte read or -1 if EOF was reached.
     * @throws IOException
     */
    public static int read(InputStream inputStream) throws IOException {
        byte[] res = new byte[1];
        int read = inputStream.read(res);
        if (read == -1) {
            return -1;
        }
        return res[0];
    }

    public static void closeQuietly(Closeable is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // Close failed.
            }
        }
    }

}
