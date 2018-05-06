/* Copyright 2016 (c) Sepior Aps, all rights reserved. */

package com.sepior.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import static sun.misc.Version.print;

/**
 * Modify this class to achieve parallelism on encryption.
 */
public final class ConcurrentOutputStream extends OutputStream {
    private final OutputStream stream;
    private Semaphore isDoneSemaphore;
    private ConcurrentLinkedQueue<Thread> threadConcurrentLinkedQueue;
    private ConcurrentLinkedQueue<byte[]> bufferConcurrentLinkedQueue;

    public ConcurrentOutputStream(OutputStream stream) {
        this.stream = stream;
        isDoneSemaphore = new Semaphore(1);
        threadConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        bufferConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[]{(byte) b};
        write(bytes);
    }

    private Thread createThread(final byte[] b, final int off, final int len){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (stream){
                        stream.write(b, off, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(threadConcurrentLinkedQueue.isEmpty())
                    isDoneSemaphore.release();
                else
                    threadConcurrentLinkedQueue.poll().start();
            }
        });
        return thread;
    }

    @Override
    public void write(final byte[] b, int off, int len) throws IOException
    {
        boolean isNoThreadsAddedYet = threadConcurrentLinkedQueue.isEmpty();
        Thread workingThread = createThread(b.clone(), off, len);
        bufferConcurrentLinkedQueue.add(b);
        if(isNoThreadsAddedYet){
            try
            {
                isDoneSemaphore.acquire();
                workingThread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
            threadConcurrentLinkedQueue.add(workingThread);
    }

    @Override
    public void flush() throws IOException {
        try {
            isDoneSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stream.flush();
        isDoneSemaphore.release();
    }

    @Override
    public void close() throws IOException {
        try {
            isDoneSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Called after");
        stream.close();
        isDoneSemaphore.release();
    }
}
