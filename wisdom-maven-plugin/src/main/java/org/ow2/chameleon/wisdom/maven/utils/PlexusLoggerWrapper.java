package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 30/10/2013
 * Time: 10:28
 * To change this template use File | Settings | File Templates.
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
