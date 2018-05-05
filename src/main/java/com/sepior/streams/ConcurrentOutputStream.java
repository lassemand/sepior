/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static sun.misc.Version.print;

/**
 * Modify this class to achieve parallelism on encryption.
 */
public final class ConcurrentOutputStream extends OutputStream {
    private final OutputStream stream;
    private AtomicInteger blockCounter;
    private IOException exception;
    private Semaphore semaphore;
    private String uuid;

    public ConcurrentOutputStream(OutputStream stream) {
        this.stream = stream;
        blockCounter = new AtomicInteger(0);
        semaphore = new Semaphore(-1);
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public void write(int b) throws IOException {
        System.out.println("b: " + b);
        byte[] bytes = new byte[]{(byte) b};
        write(bytes);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if(len == 0)
            return;
        System.out.println(uuid + " len: " + len);
        blockCounter.getAndIncrement();
        semaphore.release();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (stream){
                        stream.write(b, off, len);
                    }
                } catch (IOException e) {
                    exception = e;
                }
                boolean isLastElement = blockCounter.getAndDecrement() == 1;
                if(isLastElement){
                    semaphore.release();
                    System.out.println("final UUID released: " + uuid);
                }
            }
        });
        thread.start();
        try {
            semaphore.acquire();
            System.out.println("UUID acquire: " + uuid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(exception != null)
            throw exception;
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
