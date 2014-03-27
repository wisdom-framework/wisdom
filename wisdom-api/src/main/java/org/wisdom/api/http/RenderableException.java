package org.wisdom.api.http;

/**
 * Exception thrown when a content cannot be rendered correctly.
 */
public class RenderableException extends Exception {

    public RenderableException(String message) {
        super(message);
    }

    public RenderableException(String message, Exception cause) {
        super(cause);
    }
}
