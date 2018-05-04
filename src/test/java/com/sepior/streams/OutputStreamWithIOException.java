/* Copyright (c) 2013-2015 Sepior Aps, all rights reserved. */
package com.sepior.streams;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class used for testing OutputStream implementations.
 * @author mv
 *
 */
public class OutputStreamWithIOException extends OutputStream{

    @Override
    public void write(int b) throws IOException {
        throw new IOException("Something terrible happened.");
    }

}
