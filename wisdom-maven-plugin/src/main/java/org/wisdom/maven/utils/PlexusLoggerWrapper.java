package org.wisdom.maven.utils;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

/**
 * Wrap the Maven logger within a Plexus Logger.
 */
public class PlexusLoggerWrapper extends AbstractLogger {
    private final Log log;

    public PlexusLoggerWrapper(Log log) {
        super(Logger.LEVEL_INFO, log.toString());
        this.log = log;
    }

    @Override
    public void debug(String message, Throwable throwable) {
        log.debug(message, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        log.info(message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log.warn(message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    @Override
    public Logger getChildLogger(String name) {
        return this;
    }
}
