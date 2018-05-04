/* Copyright (c) 2013-2015 Sepior Aps, all rights reserved. */
package com.sepior.streams;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class ConcurrentOutputStreamTest {

    @Test
    public void testExceptionOnWrite() throws Exception {
        OutputStream cos = new ConcurrentOutputStream(new OutputStreamWithIOException());
        byte[] buffer = new byte[] { 1, 2, 0, -1 };

        try {
            cos.write(buffer);
            Thread.sleep(20);
            cos.write(buffer);
            fail("Should fail.");
        } catch (IOException e) {
            // Expected
        }
        cos.close();

        cos = new ConcurrentOutputStream(new OutputStreamWithIOException());
        try {
            cos.write(buffer);
            cos.write(buffer);
            cos.close();
            fail("Should fail.");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test
    public void testFlushAfterClose() throws Exception {
        OutputStream cos = new ConcurrentOutputStream(new ByteArrayOutputStream());
        byte[] buffer = new byte[] { 1, 2, 0, -1 };

        cos.write(buffer);
        cos.close();
        try {
        cos.flush(); // Should at least not hang!
        } catch (IOException ignored) {
            
        }
    }

    @Test
    public void testCloseTwice() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] data = new byte[] { 41, 1, 0, -1, 10, 12 };
        OutputStream c = new ConcurrentOutputStream(os);
        c.write(data);
        c.close();
        c.close();
        assertEquals(os.toByteArray().length, data.length);
    }

    @Test
    public void testCorrectnessRandomWrites() throws Exception {
        Random random = new Random();
        ByteArrayOutputStream nonConcurrentOs = new ByteArrayOutputStream();
        ByteArrayOutputStream wrappedOs = new ByteArrayOutputStream();
        OutputStream concurrentOs = new ConcurrentOutputStream(wrappedOs);

        int maxSize = 16 * 1024 * 1024;
        int written = 0;
        byte[] buffer = new byte[2048];
        while (written < maxSize) {
            random.nextBytes(buffer);
            int off = random.nextInt(buffer.length);
            int len = random.nextInt(buffer.length - off);
            nonConcurrentOs.write(buffer, off, len);
            concurrentOs.write(buffer, off, len);
            written += len;
        }
        concurrentOs.close();
        nonConcurrentOs.close();

        assertTrue(StreamUtils.isEqualAndClose(new ByteArrayInputStream(nonConcurrentOs.toByteArray()),
                new ByteArrayInputStream(wrappedOs.toByteArray())));

    }

    @Test
    public void testSpeedUp() throws Exception {
        // Mv: we use chaining to make sure we can utilize multiple threads.

        // Try writing without concurrency.
        OutputStream os = new SlowOutputStream(new SlowOutputStream(null));
        long timeWithoutConcurrency = writeWithCalculation(os);

        // Now write WITH concurrency.
        os = new ConcurrentOutputStream(new SlowOutputStream(new ConcurrentOutputStream(new SlowOutputStream(null))));
        long timeWithConcurrency = writeWithCalculation(os);

        // There must be a 'significant' speedup. Otherwise something is wrong.
        assertTrue(timeWithoutConcurrency > timeWithConcurrency * 1.5);

    }
    
    @Test
    public void testFlush() throws Exception {
        Random random = new Random();
        byte[] buffer = new byte[1024];
        random.nextBytes(buffer);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = new ConcurrentOutputStream(new ConcurrentOutputStream(baos));
        os.write(buffer);
        os.flush();
        byte[] done = baos.toByteArray();
        assertTrue(Arrays.equals(buffer, done));
        os.close();
        
    }

    private static long writeWithCalculation(OutputStream os) throws IOException {
        final int BUFFER_SIZE = 128;
        int size = 8 * BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];

        int written = 0;
        long before = System.currentTimeMillis();
        while (written < size) {
            os.write(buffer);
            written += buffer.length;
        }
        os.close();
        long time = System.currentTimeMillis() - before;
        return time;
    }

    private final class SlowOutputStream extends OutputStream {
        private OutputStream os;

        public SlowOutputStream(OutputStream os) {
            this.os = os;
        }

        @Override
        public void write(int b) throws IOException {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            if (os != null) {
                os.write(b);
            }
        }

    }

}
