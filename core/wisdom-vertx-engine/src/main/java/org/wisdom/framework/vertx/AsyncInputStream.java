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

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.concurrent.ExecutorService;

/**
 * Reads an input stream in an asynchronous and Vert.X compliant way.
 * Instances acts a finite state machine with 3 different states: {@literal ACTIVE, PAUSED,
 * CLOSED}. The transition between the states depends on the control flow (i.e. the pump consuming the stream).
 */
public class AsyncInputStream implements ReadStream<Buffer> {

    /**
     * PAUSED state.
     */
    public static final int STATUS_PAUSED = 0;

    /**
     * ACTIVE state.
     */
    public static final int STATUS_ACTIVE = 1;

    /**
     * CLOSED state.
     */
    public static final int STATUS_CLOSED = 2;

    /**
     * Default chunk size.
     */
    static final int DEFAULT_CHUNK_SIZE = 8192;

    /**
     * An empty byte array.
     */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * The Vert.X instance.
     */
    private final Vertx vertx;

    /**
     * The executor used to read the chunks.
     */
    private final ExecutorService executor;

    /**
     * A push back input stream wrapping the read input stream.
     */
    private final PushbackInputStream in;

    /**
     * The chunk size.
     */
    private final int chunkSize;

    /**
     * The current state.
     */
    private volatile int state = STATUS_ACTIVE;

    /**
     * The close handler invoked when the stream is completed or closed.
     */
    private Handler<Void> closeHandler;

    /**
     * The data handler receiving the data read from the stream.
     */
    private Handler<Buffer> dataHandler;

    /**
     * The failure handler called when an error is encountered while reading the stream.
     */
    private Handler<Throwable> failureHandler;

    /**
     * The number of byte read form the input stream.
     */
    private int offset;
    private Context context;

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.AsyncInputStream}. This constructor uses the default
     * chunk size.
     *
     * @param vertx    the Vert.X instance
     * @param executor the executor used to read the chunk
     * @param in       the input stream to read
     */
    public AsyncInputStream(Vertx vertx, ExecutorService executor, InputStream in) {
        this(vertx, executor, in, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.AsyncInputStream}.
     *
     * @param vertx     the Vert.X instance
     * @param executor  the executor used to read the chunk
     * @param in        the input stream to read
     * @param chunkSize the chunk size
     */
    public AsyncInputStream(Vertx vertx, ExecutorService executor, InputStream in, int chunkSize) {
        if (in == null) {
            throw new NullPointerException("in");
        }
        if (vertx == null) {
            throw new NullPointerException("vertx");
        }
        this.vertx = vertx;
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
        this.executor = executor;
    }

    /**
     * Gets the current state.
     *
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the end handler.
     *
     * @param endHandler the handler called when the stream is read completely.
     * @return the current {@link org.wisdom.framework.vertx.AsyncInputStream}
     */
    @Override
    public AsyncInputStream endHandler(Handler<Void> endHandler) {
        this.closeHandler = endHandler;
        return this;
    }

    /**
     * Set a data handler. As data is read, the handler will be called with the data.
     *
     * @param handler the handler.
     * @return a reference to this, so the API can be used fluently
     */
    @Override
    public ReadStream<Buffer> handler(Handler<Buffer> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler");
        }
        this.dataHandler = handler;
        doRead();
        return this;
    }


    /**
     * The method actually reading the stream.
     * Except the first calls, this method is executed within an Akka thread.
     */
    private void doRead() {
        if (context == null) {
            context = vertx.getOrCreateContext();
        }
        if (state == STATUS_ACTIVE) {
            final Handler<Buffer> dataHandler = this.dataHandler;
            final Handler<Void> closeHandler = this.closeHandler;
            executor.submit(
                    (Runnable) () -> {
                        try {
                            final byte[] bytes = readChunk();

                            if (bytes == null || bytes.length == 0) {
                                // null or 0 means we reach the end of the stream, invoke the close handler.
                                state = STATUS_CLOSED;
                                IOUtils.closeQuietly(in);
                                context.runOnContext(event -> {
                                    if (closeHandler != null) {
                                        closeHandler.handle(null);
                                    }
                                });
                            } else {
                                // We still have data, dispatch it.
                                context.runOnContext(event -> {
                                    dataHandler.handle(Buffer.buffer(bytes));
                                    // The next chunk will be read in another call, and maybe another thread.
                                    // As the data was already given to the data handler, this is fine.
                                    doRead();
                                });
                            }
                        } catch (final Exception e) {
                            // Error detected, invokes the failure handler.
                            state = STATUS_CLOSED;
                            IOUtils.closeQuietly(in);
                            /**
                             * Invokes the failure handler.
                             * @param event irrelevant
                             */
                            context.runOnContext(event -> {
                                if (failureHandler != null) {
                                    failureHandler.handle(e);
                                }
                            });
                        }
                    });
        }
    }

    /**
     * Pauses the reading.
     *
     * @return the current {@code AsyncInputStream}
     */
    @Override
    public AsyncInputStream pause() {
        if (state == STATUS_ACTIVE) {
            state = STATUS_PAUSED;
        }
        return this;
    }

    /**
     * Resumes the reading.
     *
     * @return the current {@code AsyncInputStream}
     */
    @Override
    public AsyncInputStream resume() {
        switch (state) {
            case STATUS_CLOSED:
                throw new IllegalStateException("Cannot resume, already closed");
            case STATUS_PAUSED:
                state = STATUS_ACTIVE;
                doRead();
        }
        return this;
    }

    /**
     * Sets the failure handler.
     *
     * @param handler the failure handler.
     * @return the current {@link org.wisdom.framework.vertx.AsyncInputStream}
     */
    @Override
    public AsyncInputStream exceptionHandler(Handler<Throwable> handler) {
        this.failureHandler = handler;
        return this;
    }

    /**
     * Retrieves the number of read bytes.
     *
     * @return the number of read bytes
     */
    public long transferredBytes() {
        return offset;
    }

    /**
     * Checks whether or not the current stream is closed.
     *
     * @return {@code true} if the current {@link org.wisdom.framework.vertx.AsyncInputStream} is in the "CLOSED" state.
     */
    public boolean isClosed() {
        return state == STATUS_CLOSED;
    }

    /**
     * Checks whether or not we reach the end of the stream.
     *
     * @return {@code true} if we read the end of the stream, {@code false} otherwise
     * @throws Exception if the stream cannot be read.
     */
    public boolean isEndOfInput() throws Exception {
        int b = in.read();
        if (b < 0) {
            return true;
        } else {
            in.unread(b);
            return false;
        }
    }

    /**
     * Reads a chunk.
     * @return the read bytes, empty if we reached the end of the stream. The returned array has exactly the sisize
     * of the chunk.
     * @throws Exception if the stream cannot be read.
     */
    private byte[] readChunk() throws Exception {
        if (isEndOfInput()) {
            return EMPTY_BYTE_ARRAY;
        }

        try {
            // transfer to buffer
            byte[] tmp = new byte[chunkSize];
            int readBytes = in.read(tmp);
            if (readBytes <= 0) {
                return null;
            }
            byte[] buffer = new byte[readBytes];
            System.arraycopy(tmp, 0, buffer, 0, readBytes);
            offset += readBytes;
            return buffer;
        } catch (IOException e) {
            // Close the stream, and propagate the exception.
            IOUtils.closeQuietly(in);
            throw e;
        }
    }

    public AsyncInputStream setContext(Context context) {
        this.context = context;
        return this;
    }
}
