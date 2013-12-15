package org.wisdom.engine.ssl;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * A class creating the SSL server context.
 */
public class SSLServerContext {

    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-engine");
    private static final String PROTOCOL = "TLS";
    private static SSLServerContext INSTANCE;
    private final SSLContext serverContext;

    /**
     * Returns the singleton instance for this class
     */
    public synchronized static SSLServerContext getInstance(File root) {
        if (INSTANCE == null) {
            INSTANCE = new SSLServerContext(root);
        }
        return INSTANCE;
    }

    /**
     * Constructor for singleton
     * @param root
     */
    private SSLServerContext(File root) {
        LOGGER.info("Configuring HTTPS support");
        String path = System.getProperty("https.keyStore");
        String ca = System.getProperty("https.trustStore");
        KeyManagerFactory kmf = null;
        TrustManager[] trust = null;
        if (path == null) {
            kmf = getFakeKeyManagerFactory(root);
            LOGGER.warn("HTTPS configured with no client " +
                    "side CA verification. Requires http://webid.info/ for client certificate verification.");
            trust = new TrustManager[] {new AcceptAllTrustManager()};
        } else {
            try {
                kmf = getKeyManagerFactoryFromKeyStore(root, path);
            } catch (KeyStoreException e) {
                throw new RuntimeException("Cannot read the key store file", e);
            }

            if (! ca.equals("noCA")) {
                LOGGER.info("Using default trust store for client side CA verification");
            } else {
                trust = new TrustManager[] {new AcceptAllTrustManager()};
                LOGGER.warn("HTTPS configured with no client " +
                        "side CA verification. Requires http://webid.info/ for client certificate verification.");
            }
        }
        try {
            SSLContext context = SSLContext.getInstance(PROTOCOL);
            context.init(kmf.getKeyManagers(), trust, null);
            serverContext = context;
        } catch (Exception e) {
            throw new RuntimeException("Failure during HTTPS initialization: " + e.getMessage(), e);
        }


    }

    /**
     * Returns the server context with server side key store
     */
    public SSLContext serverContext() {
        return serverContext;
    }

    private KeyManagerFactory getKeyManagerFactoryFromKeyStore(File maybeRoot, String path) throws KeyStoreException {
        KeyManagerFactory kmf;
        File file = new File(path);
        if (! file.isFile()) {
            // Second chance.
            file = new File(maybeRoot, path);
        }

        LOGGER.info("\t key store: " + file.getAbsolutePath());
        KeyStore keyStore = KeyStore.getInstance(System.getProperty("https.keyStoreType", "JKS"));
        LOGGER.info("\t key store type: " + keyStore.getType());
        LOGGER.info("\t key store provider: " + keyStore.getProvider());
        char[] password = System.getProperty("https.keyStorePassword", "").toCharArray();
        LOGGER.info("\t key store password length: " + password.length);
        String algorithm = System.getProperty("https.keyStoreAlgorithm", KeyManagerFactory.getDefaultAlgorithm());
        LOGGER.info("\t key store algorithm: " + algorithm);
        if (file.isFile()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
                keyStore.load(stream, password);
                kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(keyStore, password);
            } catch (Exception e) {
                throw new RuntimeException("Failure during HTTPS initialization: " + e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } else {
            throw new RuntimeException("Cannot load key store from '" + file.getAbsolutePath() + "', " +
                    "the file does not exist");
        }
        return kmf;
    }

    private KeyManagerFactory getFakeKeyManagerFactory(File root) {
        KeyManagerFactory kmf;
        LOGGER.warn("Using generated key with self signed certificate for HTTPS. This MUST not be used in " +
                "production. To  set the key store use: `-Dhttps.keyStore=my-keystore`");
        kmf = FakeKeyStore.keyManagerFactory(root);
        return kmf;
    }
}
