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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.x509.*;

import javax.net.ssl.KeyManagerFactory;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate a Fake Key Store.
 * Do not use this in production!
 */
public final class FakeKeyStore {

    public static final String KEYSTORE_PATH = "conf/fake.keystore";
    public static final String DN_NAME = "CN=localhost, OU=Testing, O=Mavericks, L=Moon Base 1, ST=Cyberspace, " +
            "C=CY";
    private static final String SHA1WITHRSA = "SHA1withRSA";
    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-vertx-engine");
    
    private FakeKeyStore(){
        //Unused
    }

    public static File generateFakeKey(File root) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            File keyStoreFile = new File(root, KEYSTORE_PATH);
            if (!keyStoreFile.exists()) {
                generateAndStoreKeyStore(keyStore, keyStoreFile);
            } else {
                loadKeyStore(keyStore, keyStoreFile);
            }
            return keyStoreFile;
        } catch (Exception e) {
            LOGGER.error("Cannot generate or read the fake key store", e);
            return null;
        }
    }

    private static void loadKeyStore(KeyStore keyStore, File keyStoreFile) throws IOException,
            NoSuchAlgorithmException, CertificateException {
        InputStream is = null;
        try {
            is = new FileInputStream(keyStoreFile);
            keyStore.load(is, "".toCharArray());
        } finally {
            IOUtils.closeQuietly(is);
        }

    }

    private static void generateAndStoreKeyStore(KeyStore keyStore, File keyStoreFile) throws Exception {
        FileOutputStream out = null;
        try {
            LOGGER.info("Generating HTTPS key pair in " + keyStoreFile.getAbsolutePath() + " - this may take some" +
                    " time. If nothing happens, try moving the mouse/typing on the keyboard to generate some entropy.");

            // Generate the key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Generate a self signed certificate
            X509Certificate cert = createSelfSignedCertificate(keyPair);

            // Create the key store, first set the store pass
            keyStore.load(null, "".toCharArray());
            keyStore.setKeyEntry("wisdom-generated", keyPair.getPrivate(), "".toCharArray(),
                    new X509Certificate[]{cert});

            keyStoreFile.getParentFile().mkdirs();
            out = new FileOutputStream(keyStoreFile);
            keyStore.store(out, "".toCharArray());

            LOGGER.info("Key Store generated in " + keyStoreFile.getAbsoluteFile());
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    @SuppressWarnings("restriction")
	private static X509Certificate createSelfSignedCertificate(KeyPair keyPair) throws Exception {
        X509CertInfo certInfo = new X509CertInfo();
        // Serial number and version
        certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())));
        certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));

        // Validity
        Date validFrom = new Date();
        Date validTo = new Date(validFrom.getTime() + 50L * 365L * 24L * 60L * 60L * 1000L);
        CertificateValidity validity = new CertificateValidity(validFrom, validTo);
        certInfo.set(X509CertInfo.VALIDITY, validity);

        // Subject & Issuer
        X500Name owner = new X500Name(DN_NAME);
        boolean justName = isJavaAtLeast(1.8);
        if (justName) {
            certInfo.set(X509CertInfo.SUBJECT, owner);
            certInfo.set(X509CertInfo.ISSUER, owner);
        } else {
            certInfo.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
            certInfo.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        }

        // Key and algorithm
        certInfo.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
        AlgorithmId algorithm = new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid);
        certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithm));

        // Create a new certificate and sign it
        X509CertImpl cert = new X509CertImpl(certInfo);
        cert.sign(keyPair.getPrivate(), SHA1WITHRSA);

        // Since the SHA1withRSA provider may have a different algorithm ID to what we think it should be,
        // we need to reset the algorithm ID, and resign the certificate
        AlgorithmId actualAlgorithm = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, actualAlgorithm);
        X509CertImpl newCert = new X509CertImpl(certInfo);
        newCert.sign(keyPair.getPrivate(), SHA1WITHRSA);

        return newCert;


    }

    public static final Pattern JAVA_VERSION = Pattern.compile("([0-9]*.[0-9]*)(.*)?");

    /**
     * Checks whether the current JAva runtime has a version equal or higher then the given one. As Java version are
     * not double (because they can use more digits such as 1.8.0), this method extracts the two first digits and
     * transforms it as a double.
     * @param version the version
     * @return {@literal true} if the current Java runtime is at least the specified one,
     * {@literal false} if not or if the current version cannot be retrieve or is the retrieved version cannot be
     * parsed as a double.
     */
    public static boolean isJavaAtLeast(double version) {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion == null) {
            return false;
        }

        // if the retrieved version is one three digits, remove the last one.
        Matcher matcher = JAVA_VERSION.matcher(javaVersion);
        if (matcher.matches()) {
            javaVersion = matcher.group(1);
        }

        try {
            double v = Double.parseDouble(javaVersion);
            return v >= version;
        } catch (NumberFormatException e) { //NOSONAR if it's not a number, just return false
            return false;
        }
    }
}
