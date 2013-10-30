package org.ow2.chameleon.wisdom.maven.processors;

/**
* Processors can throw this exception when something wrong happens during the processing.
*/
public class ProcessorException extends Exception {

    private static final long serialVersionUID = 1421637223171144784L;

    public ProcessorException(String message) {
        super(message);
    }
    public ProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
