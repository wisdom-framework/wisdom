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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * An implementation of the crypto service.
 * <p/>
 * This implementation can be configured from the `conf/application.conf` file:
 * <ul>
 * <li><code>crypto.default.hash</code>: the default Hash algorithm among SHA1, SHA-256, SHA-512 and MD5 (default).</li>
 * <li><code>aes.key.size</code>: the key size used in AES with CBC methods. 128 is used by default. Be aware
 * the 256+ keys require runtime adaption because of legal limitations (see unlimited crypto package JCE)</li>
 * <li><code>aes.iterations</code>: the number of iterations used to generate the key (20 by default)</li>
 * </ul>
 */
@Component
@Provides
@Instantiate(name = "crypto")
public class CryptoServiceSingleton implements Crypto {

    public static final String AES_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String AES_ECB_ALGORITHM = "AES";
    private static final Charset UTF_8 = Charsets.UTF_8;
    public static final String HMAC_SHA_1 = "HmacSHA1";
    public static final String PBKDF_2_WITH_HMAC_SHA_1 = "PBKDF2WithHmacSHA1";

    private int keySize;
    private int iterationCount;
    private Hash defaultHash;

    private final String secret;

    @SuppressWarnings("UnusedDeclaration")
    public CryptoServiceSingleton(@Requires ApplicationConfiguration configuration) {
        this(
                configuration.getOrDie(ApplicationConfiguration.APPLICATION_SECRET),
                Hash.valueOf(configuration.getWithDefault("crypto.default.hash", "MD5")),
                configuration.getIntegerWithDefault("aes.key.size", 128),
                configuration.getIntegerWithDefault("aes.iterations", 20));
    }

    public CryptoServiceSingleton(String secret, Hash defaultHash,
                                  Integer keySize, Integer iterationCount) {
        this.secret = secret;
        this.defaultHash = defaultHash;
        this.keySize = keySize;
        this.iterationCount = iterationCount;
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
            byte[] raw = Hex.decodeHex(salt.toCharArray());
            KeySpec spec = new PBEKeySpec(privateKey.toCharArray(), raw, iterationCount, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA_1);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), AES_ECB_ALGORITHM);
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
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
        return new String(Base64.encodeBase64(encrypted));
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
            byte[] raw = Hex.decodeHex(vector.toCharArray());
            Cipher cipher = Cipher.getInstance(AES_CBC_ALGORITHM);
            cipher.init(encryptMode, generatedKey, new IvParameterSpec(raw));
            return cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | DecoderException | InvalidKeyException |
                InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Sign a message using the application secret key (HMAC-SHA1)
     */
    @Override
    public String sign(String message) {
        return sign(message, secret.getBytes());
    }

    /**
     * Sign a message with a key
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
            byte[] rawHmac = mac.doFinal(message.getBytes());

            // Convert raw bytes to Hex
            byte[] hexBytes = new Hex().encode(rawHmac);

            // Covert array of Hex bytes to a String
            return new String(hexBytes, UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a hash using the default hashing algorithm
     *
     * @param input The password
     * @return The password hash
     */
    @Override
    public String hash(String input) {
        return hash(input, defaultHash);
    }

    /**
     * Create a hash using specific hashing algorithm
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
            byte[] out = m.digest(input.getBytes());
            return new String(Base64.encodeBase64(out));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
            return Hex.encodeHexString(cipher.doFinal(value.getBytes()));
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
            return new String(cipher.doFinal(Hex.decodeHex(value.toCharArray())));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException | DecoderException e) {
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
        return String.valueOf(Hex.encodeHex(secret.substring(16, 32).getBytes()));
    }

    /**
     * Sign a token.  This produces a new token, that has this token signed with a nonce.
     * <p/>
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
        Preconditions.checkState(chunks.length == 3);
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
     * <p/>
     * Given a length that both Strings are equal to, this method will always run in constant time.
     * This prevents timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
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
     * Encode binary data to base64
     *
     * @param value The binary data
     * @return The base64 encoded String
     */
    @Override
    public String encodeBase64(byte[] value) {
        return new String(Base64.encodeBase64(value));
    }

    /**
     * Decode a base64 value
     *
     * @param value The base64 encoded String
     * @return decoded binary data
     */
    @Override
    public byte[] decodeBase64(String value) {
        return Base64.decodeBase64(value.getBytes(UTF_8));
    }

    /**
     * Build an hexadecimal MD5 hash for a String
     *
     * @param value The String to hash
     * @return An hexadecimal Hash
     */
    @Override
    public String hexMD5(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(Hash.MD5.toString());
            messageDigest.reset();
            messageDigest.update(value.getBytes(UTF_8));
            byte[] digest = messageDigest.digest();
            return String.valueOf(Hex.encodeHex(digest));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Build an hexadecimal SHA1 hash for a String
     *
     * @param value The String to hash
     * @return An hexadecimal Hash
     */
    @Override
    public String hexSHA1(String value) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance(Hash.SHA1.toString());
            md.update(value.getBytes(UTF_8));
            byte[] digest = md.digest();
            return String.valueOf(Hex.encodeHex(digest));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}