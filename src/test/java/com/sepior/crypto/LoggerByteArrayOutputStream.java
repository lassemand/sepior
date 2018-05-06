package com.sepior.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by lasse on 5/5/2018.
 */
public class LoggerByteArrayOutputStream extends ByteArrayOutputStream {

    private String uniqueIdentifier;

    public LoggerByteArrayOutputStream() {
        uniqueIdentifier = UUID.randomUUID().toString();
    }

    public LoggerByteArrayOutputStream(int size) {
        super(size);
        uniqueIdentifier = UUID.randomUUID().toString();
    }

    @Override
    public synchronized void write(int b) {
        super.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        String test = "";
        for (byte ba: b) {
            test += ba + " ";
        }
        System.out.println(uniqueIdentifier + " sync bytes: " + test);
        System.out.println(uniqueIdentifier + " sync off: " + off);
        System.out.println(uniqueIdentifier + " sync len: " + len);
        super.write(b, off, len);
    }

    @Override
    public synchronized void reset() {
        super.reset();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }
}
