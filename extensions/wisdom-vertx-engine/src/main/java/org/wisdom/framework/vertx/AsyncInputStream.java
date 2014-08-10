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
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.streams.ReadStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.concurrent.Executor;

/**
 * Created by clement on 08/08/2014.
 */
public class AsyncInputStream implements ReadStream<AsyncInputStream> {

    public static final int STATUS_PAUSED = 0, STATUS_ACTIVE = 1, STATUS_CLOSED = 2;
    static final int DEFAULT_CHUNK_SIZE = 8192;

    private final Vertx vertx;
    private final Executor executor;
    private final PushbackInputStream in;
    private final int chunkSize;

    private int status = STATUS_ACTIVE;

    private Handler<Void> closeHandler;
    private Handler<Buffer> dataHandler;
    private Handler<Throwable> failureHandler;

    private int offset;

    public AsyncInputStream(Vertx vertx, Executor executor, InputStream in) {
        this(vertx, executor, in, DEFAULT_CHUNK_SIZE);
    }

    public AsyncInputStream(Vertx vertx, Executor executor, InputStream in, int chunkSize) {
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

    public int getStatus() {
        return status;
    }

    @Override
    public AsyncInputStream endHandler(Handler<Void> endHandler) {
        this.closeHandler = endHandler;
        return this;
    }

    @Override
    public AsyncInputStream dataHandler(Handler<Buffer> handler) {
        if (handler == null) {
            throw new UnsupportedOperationException("not implemented");
        }
        this.dataHandler = handler;
        doRead();
        return this;
    }

    private void doRead() {
        if (status == STATUS_ACTIVE) {
            final Handler<Buffer> dataHandler = this.dataHandler;
            final Handler<Void> closeHandler = this.closeHandler;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final byte[] bytes = readChunk();

                        if (bytes == null  || bytes.length == 0) {
                            status = STATUS_CLOSED;
                            vertx.runOnContext(new Handler<Void>() {
                                @Override
                                public void handle(Void event) {
                                    if (closeHandler != null) {
                                        closeHandler.handle(null);
                                    }
                                }
                            });
                        } else {
                            vertx.runOnContext(new Handler<Void>() {
                                @Override
                                public void handle(Void event) {
                                    dataHandler.handle(new Buffer(bytes));
                                    doRead();
                                }
                            });
                        }
                    } catch (final Exception e) {
                        status = STATUS_CLOSED;
                        IOUtils.closeQuietly(in);
                        vertx.runOnContext(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                if (failureHandler != null) {
                                    failureHandler.handle(e);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public AsyncInputStream pause() {
        if (status == STATUS_ACTIVE) {
            status = STATUS_PAUSED;
        }
        return this;
    }

    @Override
    public AsyncInputStream resume() {
        switch (status) {
            case STATUS_CLOSED:
                throw new IllegalStateException();
            case STATUS_PAUSED:
                status = STATUS_ACTIVE;
                doRead();
        }
        return this;
    }

    @Override
    public AsyncInputStream exceptionHandler(Handler<Throwable> handler) {
        this.failureHandler = handler;
        return this;
    }

    public long transferredBytes() {
        return offset;
    }

    public boolean isClosed() {
        return status == STATUS_CLOSED;
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
}
