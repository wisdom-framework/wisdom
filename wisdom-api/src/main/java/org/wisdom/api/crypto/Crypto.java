package org.wisdom.api.crypto;

/**
 * A service to access some convenient cryptography and hashing utilities.
 */
public interface Crypto {

    /**
     * Sign a message using the application secret key (HMAC-SHA1)
     */
    public String sign(String message);

    /**
     * Sign a message with a key
     * @param message The message to sign
     * @param key The key to use
     * @return The signed message (in hexadecimal)
     * @throws java.lang.Exception
     */
    public String sign(String message, byte[] key);

    /**
     * Create a hash using the default hashing algorithm
     * @param input The password
     * @return The password hash
     */
    public String hash(String input);

    /**
     * Create a hash using specific hashing algorithm
     * @param input The password
     * @param hashType The hashing algorithm
     * @return The password hash
     */
    public String hash(String input, Hash hashType);

    /**
     * Encrypt a String with the AES encryption standard using the default secret (the application secret)
     * @param value The String to encrypt
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value);

    /**
     * Encrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
     * @param value The String to encrypt
     * @param privateKey The key used to encrypt
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value, String privateKey);

    /**
     * Decrypt a String with the AES encryption standard using the default secret (the application secret)
     * @param value An hexadecimal encrypted string
     * @return The decrypted String
     */
    public String decryptAES(String value);

    /**
     * Decrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
     * @param value An hexadecimal encrypted string
     * @param privateKey The key used to encrypt
     * @return The decrypted String
     */
    public String decryptAES(String value, String privateKey);

    /**
     * Sign a token.  This produces a new token, that has this token signed with a nonce.
     *
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


    String encodeBASE64(byte[] value);

    byte[] decodeBASE64(String value);

    String hexMD5(String value);

    String hexSHA1(String value);
}
