package org.wisdom.wamp.services;

/**
 * Exception thrown when an error in the WAMP registry occurs.
 */
public class RegistryException extends Exception {
    public RegistryException(String message) {
        super(message);
    }
}
