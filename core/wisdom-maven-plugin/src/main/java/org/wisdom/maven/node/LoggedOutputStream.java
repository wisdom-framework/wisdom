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
package org.wisdom.maven.node;

import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream delegating to a logger.
 */
public class LoggedOutputStream extends OutputStream {

    /**
     * The logger.
     */
    private final Log log;

    /**
     * Sets to true if we need to use the 'warn' method.
     */
    private final boolean useWarn;

    /**
     * The internal memory for the written bytes.
     */
    private String mem;


    public LoggedOutputStream(Log log, boolean useWarn) {
        this.log = log;
        this.useWarn = useWarn;
        this.mem = "";
    }


    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     */
    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (b & 0xff);
        mem = mem + new String(bytes);

        if (mem.endsWith("\n")) {
            mem = mem.substring(0, mem.length() - 1);
            flush();
        }
    }

    /**
     * Flushes the output stream.
     */
    public void flush () {
        if (useWarn) {
            log.warn(mem);
        } else {
            log.info(mem);
        }
        mem = "";
    }
}
