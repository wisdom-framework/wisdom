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
package org.wisdom.framework.vertx.ssl;

import io.vertx.core.net.JksOptions;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.framework.vertx.ServiceAccessor;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * A class creating the SSL server context.
 */
public final class SSLServerContext {

    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-vertx-engine");

    private static final String HTTPSWARN = "HTTPS configured with no client " +
            "side CA verification. Requires http://webid.info/ for client certificate verification.";
    private static final String HTTPSFAIL = "Failure during HTTPS initialization";

    public static JksOptions getKeyStoreOption(final ServiceAccessor accessor) {
        LOGGER.info("Configuring HTTPS support");
        final File root = accessor.getConfiguration().getBaseDir();
        final String path = accessor.getConfiguration().get("https.keyStore");

        JksOptions options = new JksOptions();
        if (path == null) {
            File file = FakeKeyStore.generateFakeKey(root);
            LOGGER.warn(HTTPSWARN);
            return options.setPath(file.getAbsolutePath())
                .setPassword("");
        } else {
            File file = new File(path);
            if (!file.isFile()) {
                // Second chance.
                file = new File(root, path);
            }
            final char[] password = accessor.getConfiguration().getWithDefault("https.keyStorePassword", "")
                    .toCharArray();
            LOGGER.info("\t key store: " + file.getAbsolutePath());
            LOGGER.info("\t key store password length: " + password.length);
            return options.setPath(file.getPath()).setPassword(new String(password));
        }
    }

    public static JksOptions getTrustStoreOption(final  ServiceAccessor accessor) {
        final File root = accessor.getConfiguration().getBaseDir();
        final String ca = accessor.getConfiguration().get("https.trustStore");

        if (ca == null) {
            LOGGER.info("Using default trust store for client side CA verification");
            return null;
        } else if ("noCA".equalsIgnoreCase(ca)) {
            //TODO
            LOGGER.info("Using default trust store for client side CA verification - noCA");
            return null;
        } else {
            File file = new File(ca);
            if (!file.isFile()) {
                // Second chance.
                file = new File(root, ca);
            }
            LOGGER.info("\t trust store: " + file.getAbsolutePath());
            final char[] password = accessor.getConfiguration()
                    .getWithDefault("https.trustStorePassword", "").toCharArray();
            LOGGER.info("\t trust store password length: " + password.length);
            return new JksOptions().setPath(file.getAbsolutePath()).setPassword(new String(password));
        }
    }


    /**
     * Reset the SSL Context instance. For testing purpose only.
     */
    @Deprecated
    public static synchronized void reset() {
    }

}
