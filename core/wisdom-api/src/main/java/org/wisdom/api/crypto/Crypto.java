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
package org.wisdom.api.crypto;

/**
 * A service to access some convenient cryptography and hashing utilities.
 */
public interface Crypto {

    /**
     * Default AES Transformation: AES/CBC/PKCS5Padding.
     */
    public static String AES_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Sign a message using the application secret key (HMAC-SHA1).
     *
     * @param message the message to sign, must not be {@literal null}
     * @return the signed message
     */
    public String sign(String message);

    /**
     * Sign a message with a key.
     *
     * @param message The message to sign
     * @param key     The key to use
     * @return The signed message (in hexadecimal)
     */
    public String sign(String message, byte[] key);

    /**
     * Create a hash using the default hashing algorithm.
     *
     * @param input The password
     * @return The password hash
     */
    public String hash(String input);

    /**
     * Create a hash using specific hashing algorithm.
     *
     * @param input    The password
     * @param hashType The hashing algorithm
     * @return The password hash
     */
    public String hash(String input, Hash hashType);

    /**
     * Encrypt a String with the AES standard encryption (using the ECB mode) using the default secret (the
     * application secret).
     *
     * @param value The String to encrypt
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value);

    /**
     * Encrypt a String with the AES standard encryption (using the ECB mode). Private key must have a length of 16 bytes.
     *
     * @param value      The String to encrypt
     * @param privateKey The key used to encrypt
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value, String privateKey);

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
    public String encryptAESWithCBC(String value, String privateKey, String salt, String iv);

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
    public String encryptAESWithCBC(String value, String salt);

    /**
     * Decrypt a String with the standard AES encryption (using the ECB mode) using the default secret (the
     * application secret).
     *
     * @param value An hexadecimal encrypted string
     * @return The decrypted String
     */
    public String decryptAES(String value);

    /**
     * Decrypt a String with the standard AES encryption (using the ECB mode). Private key must have a length of 16
     * bytes.
     *
     * @param value      An hexadecimal encrypted string
     * @param privateKey The key used to encrypt
     * @return The decrypted String
     */
    public String decryptAES(String value, String privateKey);

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
    public String decryptAESWithCBC(String value, String privateKey, String salt, String iv);

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
    public String decryptAESWithCBC(String value, String salt);

    /**
     * Sign a token.  This produces a new token, that has this token signed with a nonce.
     * <p>
     * This primarily exists to defeat the BREACH vulnerability, as it allows the token to effectively be random per
     * request, without actually changing the value.
     *
     * @param token The token to sign
     * @return The signed token
     */
    public String signToken(String token);

    /**
     * Extract a signed token that was signed by {@link #signToken(String)}.
     *
     * @param token The signed token to extract.
     * @return The verified raw token, or null if the token isn't valid.
     */
    public String extractSignedToken(String token);


    /**
     * Encode the given byte array using Base64.
     *
     * @param value the data to encode
     * @return the base64 String
     */
    public String encodeBase64(byte[] value);

    /**
     * Decode the value (encoded using Base64).
     *
     * @param value the value to decode
     * @return the decoded data
     */
    public byte[] decodeBase64(String value);

    /**
     * Encode the given String using the MD5 Hash algorithm and return the Hex form of the result.
     *
     * @param value the value to encode
     * @return the encoded value. The value is encoded is using MD5.
     */
    public String hexMD5(String value);

    /**
     * Encode the given String using the SHA1 Hash algorithm and return the Hex form of the result.
     *
     * @param value the value to encode
     * @return the encoded value. The value is encoded is using SHA1.
     */
    public String hexSHA1(String value);

    /**
     * Generates a cryptographically secure token.
     *
     * @return the token
     */
    public String generateToken();

    /**
     * Generates a signed token.
     *
     * @return the token
     */
    public String generateSignedToken();

    /**
     * Compares two signed tokens.
     *
     * @param tokenA the first token
     * @param tokenB the second token
     * @return {@code true} if the tokens are equals, {@code false} otherwise
     */
    public boolean compareSignedTokens(String tokenA, String tokenB);

    /**
     * Computes the MD5 hash of the given String.
     *
     * @param toHash the string to hash
     * @return the MD5 hash
     */
    public byte[] md5(String toHash);

    /**
     * Computes the SHA1 hash of the given String.
     *
     * @param toHash the string to hash
     * @return the SHA1 hash
     */
    public byte[] sha1(String toHash);

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     *
     * @param bytes the bytes
     * @return the bytes in hexadecimal
     */
    public char[] hex(byte[] bytes);

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order.
     *
     * @param bytes the bytes
     * @return the hexadecimal String
     */
    public String hexToString(byte[] bytes);

    /**
     * Constant time equals method.
     * <p>
     * Given a length that both Strings are equal to, this method will always run in constant time.  This prevents
     * timing attacks.
     *
     * @param a the first string
     * @param b the second string
     * @return {@code true} if the two strings are equal, {@code false} otherwise
     */
    public boolean constantTimeEquals(String a, String b);

    /**
     * Converts an array of characters representing hexadecimal values into an array of bytes of those same values. The
     * returned array will be half the length of the passed array, as it takes two characters to represent any given
     * byte. An exception is thrown if the passed char array has an odd number of elements.
     *
     * @param value An array of characters containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied char array.
     * @throws java.lang.IllegalArgumentException Thrown if an odd number or illegal of characters is supplied
     */
    public byte[] decodeHex(String value);
}
