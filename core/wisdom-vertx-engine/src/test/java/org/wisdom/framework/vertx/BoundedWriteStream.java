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


import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

import java.util.LinkedList;

public class BoundedWriteStream implements WriteStream<Buffer> {


  private final LinkedList<byte[]> buffers = new LinkedList<>();
  private int maxSize;
  private int size = 0;
  private Handler<Void> drainHandler;

  public BoundedWriteStream() {
    this(1024);
  }

  public BoundedWriteStream(int maxSize) {
    this.maxSize = maxSize;
  }


  @Override
  public WriteStream<Buffer> write(Buffer data) {
    byte[] buffer = data.getBytes();
    buffers.addLast(buffer);
    size += buffer.length;
    return this;
  }


  public byte[] drain() {
    byte[] bytes = new byte[size];
    int offset = 0;
    while (buffers.size() > 0) {
      byte[] buffer = buffers.removeFirst();
      System.arraycopy(buffer, 0, bytes, offset, buffer.length);
      offset += buffer.length;
      size -= buffer.length;
    }
    if (drainHandler != null) {
      drainHandler.handle(null);
    }
    return bytes;
  }

  @Override
  public BoundedWriteStream setWriteQueueMaxSize(int maxSize) {
    this.maxSize = maxSize;
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return size >= maxSize;
  }

  @Override
  public BoundedWriteStream drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    return this;
  }

  @Override
  public BoundedWriteStream exceptionHandler(Handler<Throwable> handler) {
    throw new UnsupportedOperationException("not implemented");
  }
}
