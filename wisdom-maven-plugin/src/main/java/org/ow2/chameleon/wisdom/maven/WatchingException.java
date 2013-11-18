package org.ow2.chameleon.wisdom.maven;

import java.io.File;

/**
 * Common exception thrown by Watcher.
 */
public class WatchingException extends Exception {

    private File file;

    public WatchingException(String message) {
        super(message);
    }

    public WatchingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WatchingException(String message, File file, Throwable cause) {
        super(message, cause);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
