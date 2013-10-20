package org.ow2.chameleon.wisdom.api.bodies;

import java.io.File;

/**
 * Handle the request body a raw bytes data.
 */
public abstract class RawBuffer {

    /**
     * Buffer size.
     */
    public abstract Long size();

    /**
     * Returns the buffer content as a bytes array.
     *
     * @param maxLength The max length allowed to be stored in memory.
     * @return null if the content is too big to fit in memory.
     */
    public abstract byte[] asBytes(int maxLength);

    /**
     * Returns the buffer content as a bytes array.
     */
    public abstract byte[] asBytes();

    /**
     * Returns the buffer content as File.
     */
    public abstract File asFile();

}
