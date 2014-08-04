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
package org.wisdom.monitor.extensions.terminal;

import org.wisdom.api.http.websockets.Publisher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.wisdom.monitor.extensions.terminal.OutputType.RESULT;

/**
 * The Wisit terminal OutputStream create CommandResult from gogo shell command result and stream.
 *
 * @author Jonathan M. Bardin
 */
public class WisitOutputStream extends OutputStream {

    private final Publisher publisher;
    private final String topic;
    private final OutputType myType;
    private final Object lock = new Object();

    private static final String UTF8 = "UTF-8";

    public WisitOutputStream(final Publisher publisher, final String topic) {
        this(publisher, topic, RESULT);
    }

    public WisitOutputStream(final Publisher publisher, final String topic,OutputType outputType) {
        if(publisher == null || topic == null || outputType == null){
            throw new IllegalArgumentException("publisher, topic and outputType cannot be null");
        }

        this.publisher = publisher;
        this.topic = topic;
        this.myType = outputType;
    }

    @Override
    public void write(int i) throws IOException { 
        //Unused
    }

    @Override
    public void write(byte[] b) throws IOException {
        publish(new String(b, UTF8));
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        //ignore blank print
        if (len == 1 && buf[off] == 10) { 
            return;
        }

        publish(new String(buf, off, len, Charset.forName(UTF8)));
    }

    /**
     * @return The type of output
     */
    public OutputType getType(){
        return myType;
    }

    /**
     * Use the Publisher in order to broadcast the command result through the web-socket.
     *
     * @param buffer the buffer in which data is written
     */
    private void publish(String buffer){
        CommandResult out = new CommandResult(myType);

        out.setContent(buffer);

        synchronized (lock) {
            publisher.publish(topic, out.toString());
        }
    }
}
