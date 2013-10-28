package org.ow2.chameleon.wisdom.api.http;

import java.io.InputStream;

/**
 * Represents an uploaded file.
 * This class represents a file or form item that was received within a <code>multipart/form-data</code> <em>POST</em>
 * request.
 */
public interface FileItem {

    /**
     * The field name from the form.
     *
     * @return the name of the input element from the form having uploaded the file. It can be {@literal null} if the
     *         file was not uploaded from a form.
     */
    String field();

    /**
     * The name of the file
     *
     * @return the file name
     */
    String name();

    /**
     * Gets the byte
     *
     * @return the full content of the file.
     */
    byte[] bytes();

    /**
     * Opens an input stream on the file.
     *
     * @return an input stream to read the content of the uploaded item.
     */
    InputStream stream();

    /**
     * Provides a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@literal true} if the file content is in memory.
     */
    boolean isInMemory();

    /**
     * Gets the file mime type as passed by the browser.
     *
     * @return the mime type of the file, {@literal null} is not set.
     */
    String mimetype();

    /**
     * Gets the size of the uploaded item.
     *
     * @return the size of the uploaded file.
     */
    long size();
}
