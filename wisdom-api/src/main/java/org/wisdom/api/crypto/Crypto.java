package org.wisdom.api.crypto;

/**
 * A service to access some convenient cryptography and hashing utilities.
 */
public interface Crypto {
	
	
	 /**
     * Generate an random Salt to encrypt
     * @param length		An integer to specify generated salt
     * @return an hexadecimal generated String
     */
    public String randomSalt(int length);

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
     * @param input 	The password
     * @param hashType 	The hashing algorithm
     * @return The password hash
     */
    public String hash(String input, Hash hashType);

    /**
     * Encrypt a String with the AES standard encryption.
     * -> Using the default parameters (the application secret and iv).
     * @param value 	The String to encrypt
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value);

    /**
     * Encrypt a String with the AES standard encryption. 
     * -> Private key must have a length of 16 bytes.
     * -> Initialization Vector must be valid hexadecimal Strings.
     * @param value 		The String to encrypt
     * @param privateKey 	The key used to encrypt
     * @param iv		 	The initialization vector
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value, String privateKey, String iv);

    /**
     * Encrypt a String with the AES advanced encryption.
     * -> Generate new key with randomSalt method. Salt parameter will be dynamic for each encryption. Must be valid hexadecimal Strings.
     * -> Using the default parameters (the application secret and iv).
     * @param value 		The message to encrypt
     * @param salt 			The random salt hexadecimal String
     * @return encrypted 	String encoded using Base64
     */
    public String encryptSaltAES(String value, String salt);

    /**
     * Encrypt a String with the AES advanced encryption 
     * -> Generate new key with randomSalt method. Salt parameter will be dynamic for each encryption. Must be valid hexadecimal Strings.
	 * -> The Private key must have a length of 16 bytes.
     * -> The Initialization Vector must be valid hexadecimal Strings.
     * @param value 		The message to encrypt
     * @param privateKey 	The private key
     * @param iv 			The initialization vector (hexadecimal String)
     * @param salt 			The random salt hexadecimal String
     * @return encrypted 	String encoded using Base64
     */
    public String encryptSaltAES(String value, String privateKey, String iv, String salt);
    
    
    
    /**
     * Decrypt a String with the AES standard encryption.
     * -> Using the default parameters (the application secret and iv).
     * @param value 	An hexadecimal encrypted string
     * @return The decrypted String
     */
    public String decryptAES(String value);

    /**
     * Decrypt a String with the AES standard encryption.
     * -> Private key must have a length of 16 bytes.
     * -> Initialization Vector must be valid hexadecimal Strings.
     * @param value 		An hexadecimal encrypted string
     * @param privateKey 	The key used to decrypt
     * @param iv		 	The initialization vector
     * @return The decrypted String
     */
    public String decryptAES(String value, String privateKey, String iv);

    
    /**
     * Decrypt a String with the AES advanced encryption.
	 * -> Using the previously generated random salt.
     * -> Using the default parameters (the application secret and iv).
     * @param value 	An encrypted String encoded using Base64
     * @param salt 		An hexadecimal String used to salt
     * @return The decrypted String
     */
    public String decryptSaltAES(String value, String salt);

    
    /**
     * Decrypt a String with the AES advanced encryption.
     * -> The Private key must have a length of 16 bytes.
     * -> The Initialization Vector must be valid hexadecimal Strings.
     * -> Using the previously generated random salt.
     * @param value 	An encrypted String encoded using Base64.
     * @param iv 		The initialization vector (hexadecimal String)
     * @param salt 		An hexadecimal String used to salt
     * @return The decrypted String
     */
    public String decryptSaltAES(String value, String privateKey, String iv, String salt);
    
    
    
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


    /**
     * Encode the given byte array using Base64.
     *
     * @param value the data to encode
     * @return the base64 String
     */
    String encodeBASE64(byte[] value);

    /**
     * Decode the value (encoded using Base64).
     *
     * @param value the value to decode
     * @return the decoded data
     */
    byte[] decodeBASE64(String value);

    /**
     * Encode the given String using the MD5 Hash algorithm and return the Hex form of the result.
     *
     * @param value the value to encode
     * @return the encoded value. The value is encoded is using MD5.
     */
    String hexMD5(String value);

    /**
     * Encode the given String using the SHA1 Hash algorithm and return the Hex form of the result.
     *
     * @param value the value to encode
     * @return the encoded value. The value is encoded is using SHA1.
     */
    String hexSHA1(String value);
}
