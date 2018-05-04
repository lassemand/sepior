/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.streams;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Modify this class to achieve parallelism on encryption.
 */
public final class ConcurrentOutputStream extends OutputStream {
    private final OutputStream stream;

    public ConcurrentOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[]{(byte) b};
        write(bytes);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

}
