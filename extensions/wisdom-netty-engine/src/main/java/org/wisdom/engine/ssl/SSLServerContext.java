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
package org.wisdom.engine.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.engine.server.ServiceAccessor;

/**
 * A class creating the SSL server context.
 */
public final class SSLServerContext {

    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-netty-engine");
    private static final String PROTOCOL = "TLS";
    private static SSLServerContext INSTANCE;
    private final SSLContext serverContext;
    private final ServiceAccessor accessor;
    
    private static final String HTTPSWARN = "HTTPS configured with no client " +
            "side CA verification. Requires http://webid.info/ for client certificate verification.";
    private static final String HTTPSFAIL = "Failure during HTTPS initialization";

    /**
     * Constructor for singleton.
     *
     * @param accessor used to access services.
     */
    private SSLServerContext(final ServiceAccessor accessor) {
        LOGGER.info("Configuring HTTPS support");
        this.accessor = accessor;
        final File root = accessor.getConfiguration().getBaseDir();
        final String path = accessor.getConfiguration().get("https.keyStore");
        final String ca = accessor.getConfiguration().get("https.trustStore");
        KeyManagerFactory kmf = null;
        TrustManager[] trusts = null;
        
        // configure keystore
        if (path == null) {
            kmf = getFakeKeyManagerFactory(root);
            LOGGER.warn(HTTPSWARN);
            trusts = new TrustManager[]{new AcceptAllTrustManager()};
        } else {
            try {
                kmf = getKeyManagerFactoryFromKeyStore(root, path);
            } catch (final KeyStoreException e) {
                throw new RuntimeException("Cannot read the key store file", e);
            }
        }
        
        // configure trustore
        if (ca == null) {
            LOGGER.info("Using default trust store for client side CA verification");
        }
        else if ("noCA".equalsIgnoreCase(ca))
        {
            trusts = new TrustManager[]{new AcceptAllTrustManager()};
            LOGGER.warn(HTTPSWARN);
        } else {
            try {
                trusts = getTrustManagerFactoryFromKeyStore(root, ca).getTrustManagers();
            } catch (final KeyStoreException e) {
                throw new RuntimeException("Cannot read the trust store file", e);
            }
        }
        
        try {
            final SSLContext context = SSLContext.getInstance(PROTOCOL);
            context.init(kmf.getKeyManagers(), trusts, null);
            serverContext = context;
        } catch (final Exception e) {
            throw new RuntimeException(HTTPSFAIL + e.getMessage(), e);
        }


    }

    /**
     * Returns the singleton instance for this class.
     */
    public static synchronized SSLServerContext getInstance(final ServiceAccessor accessor) {
        if (INSTANCE == null) {
            INSTANCE = new SSLServerContext(accessor);
        }
        return INSTANCE;
    }

    /**
     * Returns the server context with server side key store.
     */
    public SSLContext serverContext() {
        return serverContext;
    }

    private KeyManagerFactory getKeyManagerFactoryFromKeyStore(final File maybeRoot, final String path) throws KeyStoreException {
        KeyManagerFactory kmf;
        File file = new File(path);
        if (!file.isFile()) {
            // Second chance.
            file = new File(maybeRoot, path);
        }

        LOGGER.info("\t key store: " + file.getAbsolutePath());
        final KeyStore keyStore = KeyStore.getInstance(accessor.getConfiguration().getWithDefault("https.keyStoreType", "JKS"));
        LOGGER.info("\t key store type: " + keyStore.getType());
        LOGGER.info("\t key store provider: " + keyStore.getProvider());
        final char[] password = accessor.getConfiguration().getWithDefault("https.keyStorePassword", "").toCharArray();
        LOGGER.info("\t key store password length: " + password.length);
        final String algorithm = accessor.getConfiguration().getWithDefault("https.keyStoreAlgorithm", KeyManagerFactory.getDefaultAlgorithm());
        LOGGER.info("\t key store algorithm: " + algorithm);
        if (file.isFile()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
                keyStore.load(stream, password);
                kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(keyStore, password);
            } catch (final Exception e) {
                throw new RuntimeException(HTTPSFAIL + e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } else {
            throw new RuntimeException("Cannot load key store from '" + file.getAbsolutePath() + "', " +
                    "the file does not exist");
        }
        return kmf;
    }

    private KeyManagerFactory getFakeKeyManagerFactory(final File root) {
        KeyManagerFactory kmf;
        LOGGER.warn("Using generated key with self signed certificate for HTTPS. This MUST not be used in " +
                "production. To  set the key store use: `-Dhttps.keyStore=my-keystore`");
        kmf = FakeKeyStore.keyManagerFactory(root);
        return kmf;
    }
    
    private TrustManagerFactory getTrustManagerFactoryFromKeyStore(final File maybeRoot, final String path) throws KeyStoreException {
        final TrustManagerFactory tmf;
        File file = new File(path);
        if (!file.isFile()) {
            // Second chance.
            file = new File(maybeRoot, path);
        }

        LOGGER.info("\t trust store: " + file.getAbsolutePath());
        final KeyStore trustStore = KeyStore.getInstance(accessor.getConfiguration().getWithDefault("https.trustStoreType", "JKS"));
        LOGGER.info("\t trust store type: " + trustStore.getType());
        LOGGER.info("\t trust store provider: " + trustStore.getProvider());
        final char[] password = accessor.getConfiguration().getWithDefault("https.trustStorePassword", "").toCharArray();
        LOGGER.info("\t trust store password length: " + password.length);
        final String algorithm = accessor.getConfiguration().getWithDefault("https.trustStoreAlgorithm", KeyManagerFactory.getDefaultAlgorithm());
        LOGGER.info("\t trust store algorithm: " + algorithm);
        if (file.isFile()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
                trustStore.load(stream, password);
                tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(trustStore);
            } catch (final Exception e) {
                throw new RuntimeException(HTTPSFAIL + e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } else {
            throw new RuntimeException("Cannot load trust store from '" + file.getAbsolutePath() + "', " +
                    "the file does not exist");
        }
        return tmf;
    }
    
}
