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
package org.wisdom.api.configuration;

import java.io.File;

/**
 * Service interface to access application configuration.
 */
public interface ApplicationConfiguration extends Configuration {

    /**
     * The default configuration. Make sure that file exists. Otherwise the
     * application won't start up.
     */
    String CONF_FILE_LOCATION_BY_CONVENTION = "conf/application.conf";

    /**
     * The application secret key.
     */
    String APPLICATION_SECRET = "application.secret";

    /**
     * The HTTP port key.
     */
    String HTTP_PORT = "http.port";

    /**
     * The HTTPS port key.
     */
    String HTTPS_PORT = "https.port";

    /**
     * The global encoding activation key.
     */
    String ENCODING_GLOBAL = "encoding.global";

    /**
     * The global encoding default value.
     */
    boolean DEFAULT_ENCODING_GLOBAL = true;

    /**
     * The global encoding max activation size.
     * @deprecated use {@link #ENCODING_MAX_SIZE}.
     */
    @Deprecated
    String ENCODING_MAX_SIZE_OLD = "encoding.max.size";

    /**
     * The global encoding max activation size.
     */
    String ENCODING_MAX_SIZE = "encoding.max";

    /**
     * The global encoding min activation size.
     * @deprecated use {@link #ENCODING_MIN_SIZE}.
     */
    @Deprecated
    String ENCODING_MIN_SIZE_OLD = "encoding.min.size";

    /**
     * The global encoding min activation size.
     */
    String ENCODING_MIN_SIZE = "encoding.min";

    /**
     * The default max size (10Mb) disabling the encoding.
     */
    long DEFAULT_ENCODING_MAX_SIZE = 10000 * 1024;

    /**
     * The default min size (1 Kb) disabling the encoding.
     */
    long DEFAULT_ENCODING_MIN_SIZE = 1 * 1024;

    /**
     * The global url encoding activation key.
     */
    String ENCODING_URL = "encoding.url";

    boolean DEFAULT_ENCODING_URL = true;

    /**
     * The property storing the application base directory.
     */
    String APPLICATION_BASEDIR = "application.baseDir";

    /**
     * Gets the base directory of the Wisdom application.
     *
     * @return the base directory
     */
    File getBaseDir();

    /**
     * Whether we are in dev mode.
     *
     * @return True if we are in dev mode
     */
    boolean isDev();

    /**
     * Whether we are in test mode.
     *
     * @return True if we are in test mode
     */
    boolean isTest();

    /**
     * Whether we are in prod mode.
     *
     * @return True if we are in prod mode
     */
    boolean isProd();

    /**
     * Get a File property or a default value when property cannot be found in
     * any configuration file.
     * The file object is constructed using <code>new File(basedir, value)</code>.
     *
     * @param key  the key
     * @param file the default file
     * @return the file object
     */
    File getFileWithDefault(String key, String file);

    /**
     * Get a File property or a default value when property cannot be found in
     * any configuration file.
     * The file object is constructed using <code>new File(basedir, value)</code>.
     *
     * @param key  the key
     * @param file the default file
     * @return the file object
     */
    File getFileWithDefault(String key, File file);
}
