/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.maven.utils;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.testing.helpers.Stability;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.engine.WisdomEngine;

import java.io.File;
import java.util.Iterator;

/**
 * Keep a reference on a running instance of Chameleon.
 */
public class ChameleonInstanceHolder {

    private static int HTTP_PORT = -1;
    private static int HTTPS_PORT = -1;
    private static String HOST_NAME;
    private static Chameleon INSTANCE;

    private ChameleonInstanceHolder() {
        // Avoid direct instantiation.
    }

    /**
     * Gets the currently stored reference.
     *
     * @return the stored reference, may be {@code null}
     */
    public synchronized static Chameleon get() {
        return INSTANCE;
    }

    /**
     * Stores a reference on a running chameleon.
     *
     * @param chameleon the chameleon
     */
    public synchronized static void set(Chameleon chameleon) {
        if (INSTANCE != null && chameleon != null) {
            throw new IllegalStateException("A Chameleon instance is already stored");
        }
        INSTANCE = chameleon;
        if (chameleon == null) {
            // Reset metadata
            HOST_NAME = null;
            HTTP_PORT = -1;
            HTTPS_PORT = -1;
        }
    }

    public synchronized static String getHostName() throws Exception {
        if (HOST_NAME == null) {
            retrieveServerMetadata();
        }
        return HOST_NAME;
    }

    public synchronized static int getHttpPort() throws Exception {
        if (HOST_NAME == null) {
            retrieveServerMetadata();
        }
        return HTTP_PORT;
    }

    public synchronized static int getHttpsPort() throws Exception {
        if (HOST_NAME == null) {
            retrieveServerMetadata();
        }
        return HTTPS_PORT;
    }

    /**
     * Fixes the Chameleon logging configuration to write the logs in the logs/wisdom.log file instead of chameleon.log
     * file.
     *
     * @param basedir the base directory of the chameleon
     */
    public static void fixLoggingSystem(File basedir) {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            // We know that we are using logback from here.
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logbackLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
            if (logbackLogger == null) {
                return;
            }

            Iterator<Appender<ILoggingEvent>> iterator = logbackLogger.iteratorForAppenders();
            while (iterator.hasNext()) {
                Appender<ILoggingEvent> appender = iterator.next();

                if (appender instanceof AsyncAppender) {
                    appender = ((AsyncAppender) appender).getAppender("FILE");
                }

                if (appender instanceof RollingFileAppender) {
                    RollingFileAppender<ILoggingEvent> fileAppender =
                            (RollingFileAppender<ILoggingEvent>) appender;
                    String file = new File(basedir, "logs/wisdom.log").getAbsolutePath();
                    fileAppender.stop();
                    // Remove the created log directory.
                    // We do that afterwards because on Windows the file cannot be deleted while we still have a logger
                    // using it.
                    FileUtils.deleteQuietly(new File("logs"));
                    fileAppender.setFile(file);
                    fileAppender.setContext(lc);
                    fileAppender.start();
                }
            }
        }
    }

    /**
     * Methods call by the test framework to discover the server name and port.
     *
     * @throws Exception if the service is not running.
     */
    private static void retrieveServerMetadata() throws Exception {
        if (get() == null) {
            throw new IllegalStateException("Cannot retrieve the server metadata - no reference to Chameleon stored " +
                    "in the holder");
        }

        // Before checking, ensure stability.
        Stability.waitForStability(get().context());
        ServiceReference[] references = get().context().getAllServiceReferences(WisdomEngine.class.getName(), null);

        if (references == null || references.length == 0) {
            Stability.waitForStability(get().context());
            references = get().context().getAllServiceReferences(WisdomEngine.class.getName(), null);
        }

        if (references == null || references.length == 0) {
            throw new IllegalStateException("Cannot retrieve the Wisdom Engine service");
        }

        Object engine = get().context().getService(references[0]);
        HOST_NAME = (String) engine.getClass().getMethod("hostname").invoke(engine);
        HTTP_PORT = (int) engine.getClass().getMethod("httpPort").invoke(engine);
        HTTPS_PORT = (int) engine.getClass().getMethod("httpsPort").invoke(engine);
    }
}
