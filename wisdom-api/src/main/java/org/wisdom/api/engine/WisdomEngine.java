package org.wisdom.api.engine;

/**
 * A service interface exposed by the Wisdom engine.
 * This service is used  to retrieve basic information about the engine like the hostname and the ports.
 */
public interface WisdomEngine {

    /**
     * Gets the server hostname listened by the engine.
     * @return the host name.
     */
    public String hostname();

    /**
     * Gets the HTTP port listened by the engine.
     * @return the http port, {@literal -1} if not listened
     */
    public int httpPort();

    /**
     * Gets the HTTPS port listened by the engine.
     * @return the https port, {@literal -1} if not listened
     */
    public int httpsPort();

}
