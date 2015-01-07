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
package org.wisdom.crypto;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.crypto.Hash;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * An implementation of the crypto service.
 * <p>
 * This implementation can be configured from the `conf/application.conf` file:
 * <ul>
 * <li><code>crypto.default-hash</code>: the default Hash algorithm among SHA1, SHA-256, SHA-512 and MD5 (default).</li>
 * <li><code>crypto-aes.key-size</code>: the key size used in AES with CBC methods. 128 is used by default. Be aware
 * the 256+ keys require runtime adaption because of legal limitations (see unlimited crypto package JCE)</li>
 * <li><code>crypto.aes.iterations</code>: the number of iterations used to generate the key (20 by default)</li>
 * </ul>
 */
@Component
@Provides
@Instantiate(name = "crypto")
public class CryptoServiceSingleton implements Crypto {

    public static final String AES_ECB_ALGORITHM = "AES";
    private static final Charset UTF_8 = Charsets.UTF_8;
    public static final String HMAC_SHA_1 = "HmacSHA1";
    public static final String PBKDF_2_WITH_HMAC_SHA_1 = "PBKDF2WithHmacSHA1";

    private final String transformation;
    private final int keySize;
    private final int iterationCount;
    private final Hash defaultHash;
    private final String secret;
    private final SecureRandom random = new SecureRandom();

    @SuppressWarnings("UnusedDeclaration")
    public CryptoServiceSingleton(@Requires ApplicationConfiguration configuration) {
        this(
                configuration.getOrDie(ApplicationConfiguration.APPLICATION_SECRET),
                Hash.valueOf(configuration.getWithDefault("crypto.default-hash", "MD5")),
                configuration.getIntegerWithDefault("crypto.aes.key-size", 128),
                configuration.getWithDefault("crypto.aes.transformation",  AES_CBC_ALGORITHM),
                configuration.getIntegerWithDefault("crypto.aes.iterations", 20));
    }

    public CryptoServiceSingleton(String secret, Hash defaultHash,
                                  Integer keySize, String transformation, Integer iterationCount) {
        this.secret = secret;
        this.defaultHash = defaultHash;
        this.keySize = keySize;
        this.iterationCount = iterationCount;
        this.transformation = transformation;
    }


    /**
     * Generate the AES key from the salt and the private key.
     *
     * @param salt       the salt (hexadecimal)
     * @param privateKey the private key
     * @return the generated key.
     */
    private SecretKey generateAESKey(String privateKey, String salt) {
        try {
            byte[] raw = decodeHex(salt);
            KeySpec spec = new PBEKeySpec(privateKey.toCharArray(), raw, iterationCount, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA_1);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), AES_ECB_ALGORITHM);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Encrypt a String with the AES encryption advanced using 'AES/CBC/PKCS5Padding'. Unlike the regular
     * encode/decode AES method using ECB (Electronic Codebook), it uses Cipher-block chaining (CBC). The salt must be
     * valid hexadecimal String. This method uses parts of the application secret as private key and initialization
     * vector.
     *
     * @param value The message to encrypt
     * @param salt  The salt (hexadecimal String)
     * @return encrypted String encoded using Base64
     */
    @Override
    public String encryptAESWithCBC(String value, String salt) {
        return encryptAESWithCBC(value, getSecretPrefix(), salt, getDefaultIV());
    }

    /**
     * Encrypt a String with the AES encryption advanced using 'AES/CBC/PKCS5Padding'. Unlike the regular
     * encode/decode AES method using ECB (Electronic Codebook), it uses Cipher-block chaining (CBC). The private key
     * must have a length of 16 bytes, the salt and initialization vector must be valid hex Strings.
     *
     * @param value      The message to encrypt
     * @param privateKey The private key
     * @param salt       The salt (hexadecimal String)
     * @param iv         The initialization vector (hexadecimal String)
     * @return encrypted String encoded using Base64
     */
    @Override
    public String encryptAESWithCBC(String value, String privateKey, String salt, String iv) {
        SecretKey genKey = generateAESKey(privateKey, salt);
        byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, genKey, iv, value.getBytes(UTF_8));
        return encodeBase64(encrypted);
    }

    /**
     * Decrypt a String with the AES encryption advanced using 'AES/CBC/PKCS5Padding'. Unlike the regular
     * encode/decode AES method using ECB (Electronic Codebook), it uses Cipher-block chaining (CBC). The salt and
     * initialization vector must be valid hex Strings. This method use parts of the application secret as private
     * key and the default initialization vector.
     *
     * @param value An encrypted String encoded using Base64.
     * @param salt  The salt (hexadecimal String)
     * @return The decrypted String
     */
    @Override
    public String decryptAESWithCBC(String value, String salt) {
        return decryptAESWithCBC(value, getSecretPrefix(), salt, getDefaultIV());
    }

    /**
     * Decrypt a String with the AES encryption advanced using 'AES/CBC/PKCS5Padding'. Unlike the regular
     * encode/decode AES method using ECB (Electronic Codebook), it uses Cipher-block chaining (CBC). The private key
     * must have a length of 16 bytes, the salt and initialization vector must be valid hexadecimal Strings.
     *
     * @param value      An encrypted String encoded using Base64.
     * @param privateKey The private key
     * @param salt       The salt (hexadecimal String)
     * @param iv         The initialization vector (hexadecimal String)
     * @return The decrypted String
     */
    @Override
    public String decryptAESWithCBC(String value, String privateKey, String salt, String iv) {
        SecretKey key = generateAESKey(privateKey, salt);
        byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, decodeBase64(value));
        return new String(decrypted, UTF_8);
    }

    /**
     * Utility method encrypting/decrypting the given message.
     * The sense of the operation is specified using the `encryptMode` parameter.
     *
     * @param encryptMode  encrypt or decrypt mode ({@link javax.crypto.Cipher#DECRYPT_MODE} or
     *                     {@link javax.crypto.Cipher#ENCRYPT_MODE}).
     * @param generatedKey the generated key
     * @param vector       the initialization vector
     * @param message      the plain/cipher text to encrypt/decrypt
     * @return the encrypted or decrypted message
     */
    private byte[] doFinal(int encryptMode, SecretKey generatedKey, String vector, byte[] message) {
        try {
            byte[] raw = decodeHex(vector);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(encryptMode, generatedKey, new IvParameterSpec(raw));
            return cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Sign a message using the application secret key (HMAC-SHA1).
     */
    @Override
    public String sign(String message) {
        return sign(message, secret.getBytes(Charsets.UTF_8));
    }

    /**
     * Sign a message with a key.
     *
     * @param message The message to sign
     * @param key     The key to use
     * @return The signed message (in hexadecimal)
     */
    @Override
    public String sign(String message, byte[] key) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(key);
        try {
            // Get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA_1);

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA_1);
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(message.getBytes(Charsets.UTF_8));

            // Convert raw bytes to Hex
            return hexToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Create a hash using the default hashing algorithm.
     *
     * @param input The password
     * @return The password hash
     */
    @Override
    public String hash(String input) {
        return hash(input, defaultHash);
    }

    /**
     * Create a hash using specific hashing algorithm.
     *
     * @param input    The password
     * @param hashType The hashing algorithm
     * @return The password hash
     */
    @Override
    public String hash(String input, Hash hashType) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(hashType);
        try {
            MessageDigest m = MessageDigest.getInstance(hashType.toString());
            byte[] out = m.digest(input.getBytes(Charsets.UTF_8));
            return encodeBase64(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Encrypt a String with the AES standard encryption (using the ECB mode) using the default secret (the
     * application secret).
     *
     * @param value The String to encrypt
     * @return An hexadecimal encrypted string
     */
    @Override
    public String encryptAES(String value) {
        return encryptAES(value, getSecretPrefix());
    }

    /**
     * Encrypt a String with the AES standard encryption (using the ECB mode). Private key must have a length of 16 bytes.
     *
     * @param value      The String to encrypt
     * @param privateKey The key used to encrypt
     * @return An hexadecimal encrypted string
     */
    @Override
    public String encryptAES(String value, String privateKey) {
        try {
            byte[] raw = privateKey.getBytes(UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, AES_ECB_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ECB_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return hexToString(cipher.doFinal(value.getBytes(Charsets.UTF_8)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Decrypt a String with the standard AES encryption (using the ECB mode) using the default secret (the
     * application secret).
     *
     * @param value An hexadecimal encrypted string
     * @return The decrypted String
     */
    @Override
    public String decryptAES(String value) {
        return decryptAES(value, getSecretPrefix());
    }

    /**
     * Decrypt a String with the standard AES encryption (using the ECB mode). Private key must have a length of 16
     * bytes.
     *
     * @param value      An hexadecimal encrypted string
     * @param privateKey The key used to encrypt
     * @return The decrypted String
     */
    @Override
    public String decryptAES(String value, String privateKey) {
        try {
            byte[] raw = privateKey.getBytes(UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, AES_ECB_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ECB_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return new String(cipher.doFinal(decodeHex(value)), Charsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the 16 first characters of the application secret.
     *
     * @return the secret prefix.
     */
    private String getSecretPrefix() {
        return secret.substring(0, 16);
    }

    /**
     * Gets a segment of the application secret of 16 characters and encoded them in hexadecimal. The segment
     * contains from the 16th to the 32th characters from the application secret (16 characters). The extracted
     * segment is encoded in hexadecimal
     *
     * @return the default initialization vector.
     */
    private String getDefaultIV() {
        return String.valueOf(hex(secret.substring(16, 32).getBytes(Charsets.UTF_8)));
    }

    /**
     * Sign a token.  This produces a new token, that has this token signed with a nonce.
     * <p>
     * This primarily exists to defeat the BREACH vulnerability, as it allows the token to effectively be random per
     * request, without actually changing the value.
     *
     * @param token The token to sign
     * @return The signed token
     */
    @Override
    public String signToken(String token) {
        long nonce = System.currentTimeMillis();
        String joined = nonce + "-" + token;
        return sign(joined) + "-" + joined;
    }

    /**
     * Extract a signed token that was signed by {@link #signToken(String)}.
     *
     * @param token The signed token to extract.
     * @return The verified raw token, or null if the token isn't valid.
     */
    @Override
    public String extractSignedToken(String token) {
        String[] chunks = token.split("-", 3);
        if (chunks.length != 3) {
            // Invalid format
            return null;
        }
        String signature = chunks[0];
        String nonce = chunks[1];
        String raw = chunks[2];
        if (constantTimeEquals(signature, sign(nonce + "-" + raw))) {
            return raw;
        } else {
            return null;
        }
    }

    /**
     * Constant time equals method.
     * <p>
     * Given a length that both Strings are equal to, this method will always run in constant time.
     * This prevents timing attacks.
     */
    public boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        } else {
            int equal = 0;
            for (int i = 0; i < a.length(); i++) {
                equal = equal | a.charAt(i) ^ b.charAt(i);
            }
            return equal == 0;
        }
    }

    /**
     * Encode binary data to base64.
     *
     * @param value The binary data
     * @return The base64 encoded String
     */
    @Override
    public String encodeBase64(byte[] value) {
        return new String(Base64.encodeBase64(value), Charsets.UTF_8);
    }

    /**
     * Decode a base64 value.
     *
     * @param value The base64 encoded String
     * @return decoded binary data
     */
    @Override
    public byte[] decodeBase64(String value) {
        return Base64.decodeBase64(value.getBytes(UTF_8));
    }

    /**
     * Build an hexadecimal MD5 hash for a String.
     *
     * @param value The String to hash
     * @return An hexadecimal Hash
     */
    @Override
    public String hexMD5(String value) {
        return String.valueOf(Hex.encodeHex(md5(value)));
    }

    /**
     * Build an hexadecimal SHA1 hash for a String.
     *
     * @param value The String to hash
     * @return An hexadecimal Hash
     */
    @Override
    public String hexSHA1(String value) {
        return String.valueOf(Hex.encodeHex(sha1(value)));
    }

    /**
     * Generates a cryptographically secure token.
     *
     * @return the token
     */
    @Override
    public String generateToken() {
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return hexToString(bytes);
    }

    /**
     * Generates a signed token.
     *
     * @return the token
     */
    @Override
    public String generateSignedToken() {
        return signToken(generateToken());
    }

    /**
     * Compares two signed tokens.
     *
     * @param tokenA the first token
     * @param tokenB the second token
     * @return {@code true} if the tokens are equals, {@code false} otherwise
     */
    @Override
    public boolean compareSignedTokens(String tokenA, String tokenB) {
        String a = extractSignedToken(tokenA);
        String b = extractSignedToken(tokenB);
        return a != null && b != null && constantTimeEquals(a, b);
    }

    /**
     * Computes the MD5 hash of the given String.
     *
     * @param toHash the string to hash
     * @return the MD5 hash
     */
    @Override
    public byte[] md5(String toHash) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(Hash.MD5.toString());
            messageDigest.reset();
            messageDigest.update(toHash.getBytes(UTF_8));
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // Should not happen as every JVM must support D5, SHA-1 and SHA-256.
            throw new RuntimeException(e);
        }
    }

    /**
     * Computes the SHA1 hash of the given String.
     *
     * @param toHash the string to hash
     * @return the SHA1 hash
     */
    @Override
    public byte[] sha1(String toHash) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(Hash.SHA1.toString());
            messageDigest.reset();
            messageDigest.update(toHash.getBytes(UTF_8));
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // Should not happen as every JVM must support D5, SHA-1 and SHA-256.
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * <p>
     * This method is just there to avoid consumers using commons-codec directly.
     *
     * @param bytes the bytes
     * @return the hexadecimal characters.
     */
    @Override
    public char[] hex(byte[] bytes) {
        return Hex.encodeHex(bytes);
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order.
     * <p>
     * This method is just there to avoid consumers using commons-codec directly.
     *
     * @param bytes the bytes
     * @return the hexadecimal String
     */
    @Override
    public String hexToString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    /**
     * Converts an array of characters representing hexadecimal values into an array of bytes of those same values. The
     * returned array will be half the length of the passed array, as it takes two characters to represent any given
     * byte. An exception is thrown if the passed char array has an odd number of elements.
     *
     * @param value An array of characters containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied char array.
     * @throws java.lang.IllegalArgumentException Thrown if an odd number or illegal of characters is supplied
     */
    @Override
    public byte[] decodeHex(String value) {
        try {
            return Hex.decodeHex(value.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

}