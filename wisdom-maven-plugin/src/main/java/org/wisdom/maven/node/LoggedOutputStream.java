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
     * Sets to true if we need to use the 'warn' method
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
