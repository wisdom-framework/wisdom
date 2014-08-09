/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.framework.vertx;

import org.apache.commons.io.IOUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.streams.ReadStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by clement on 08/08/2014.
 */
public class AsyncInputStream implements Runnable, ReadStream<AsyncInputStream> {

    static final int DEFAULT_CHUNK_SIZE = 8192;

    private final PushbackInputStream in;
    private final int chunkSize;
    private long offset;

    //TODO Should use a thread pool.
    private Thread thread;

    private Handler<Throwable> failureHandler;
    private Handler<Void> closeHandler;
    private Handler<Buffer> dataHandler;

    private boolean closed = false;
    private boolean pause = false;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * Creates a new instance that fetches data from the specified stream.
     */
    public AsyncInputStream(InputStream in) {
        this(in, DEFAULT_CHUNK_SIZE);
    }

    public AsyncInputStream(InputStream in, int chunkSize) {
        if (in == null) {
            throw new NullPointerException("in");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize: " + chunkSize +
                            " (expected: a positive integer)");
        }

        if (in instanceof PushbackInputStream) {
            this.in = (PushbackInputStream) in;
        } else {
            this.in = new PushbackInputStream(in);
        }
        this.chunkSize = chunkSize;
        thread = new Thread(this);
    }

    @Override
    public AsyncInputStream dataHandler(Handler<Buffer> handler) {
        this.dataHandler = handler;
        thread.start();
        return this;
    }

    public void run() {
        if (dataHandler == null) {
            throw new IllegalStateException("Data Handler null");
        }
        while (!closed) {
            try {
                // If the lock is available, we can read the data
                // The lock is not available while being paused.
                // In that case we wait for the unlock, and so we can take the lock immediately.
                if (! lock.isHeldByCurrentThread()) {
                    lock.lock();
                }
                // would be good to do some check here.
                if (closed) {
                    // We were closed -> ok
                    return;
                }

                byte[] chunk = readChunk();
                if (chunk.length == 0) {
                    // End of input.
                    invokeCloseHandler();
                    return;
                } else {
                    dataHandler.handle(new Buffer(chunk));
                }
            } catch (Exception e) {
                invokeErrorHandler(e);
                return;
            } finally {
                // Must unlock.
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

    }

    private void invokeErrorHandler(Exception e) {
        if (failureHandler != null) {
            failureHandler.handle(e);
        }
    }

    private void invokeCloseHandler() {
        if (closeHandler != null) {
            closeHandler.handle(null);
        }
    }

    public long transferredBytes() {
        return offset;
    }

    public boolean isEndOfInput() throws Exception {
        int b = in.read();
        if (b < 0) {
            return true;
        } else {
            in.unread(b);
            return false;
        }
    }

    public void close() throws Exception {
        closed = true;
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
        IOUtils.closeQuietly(in);
    }

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private byte[] readChunk() throws Exception {
        if (isEndOfInput()) {
            return EMPTY_BYTE_ARRAY;
        }

        final int availableBytes = in.available();

        final int chunkSize;
        if (availableBytes <= 0) {
            chunkSize = this.chunkSize;
        } else {
            chunkSize = Math.min(this.chunkSize, in.available());
        }

        byte[] buffer;
        try {
            // transfer to buffer
            byte[] tmp = new byte[chunkSize];
            int readBytes = in.read(tmp);
            if (readBytes <= 0) {
                return null;
            }
            buffer = tmp;
            offset += tmp.length;
            return buffer;
        } catch (IOException e) {
            IOUtils.closeQuietly(in);
            return null;
        }
    }

    @Override
    public AsyncInputStream endHandler(Handler<Void> handler) {
        this.closeHandler = handler;
        return this;
    }


    /**
     * Pause the {@code ReadSupport}. While it's paused, no data will be sent to the {@code dataHandler}
     */
    @Override
    public AsyncInputStream pause() {
        pause = true;
        if (! lock.isHeldByCurrentThread()) {
            lock.lock();
        }
        return this;
    }

    @Override
    public AsyncInputStream resume() {
        if (pause) {
            pause = false;
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return this;
    }

    @Override
    public AsyncInputStream exceptionHandler(Handler<Throwable> handler) {
        this.failureHandler = handler;
        return this;
    }
}
