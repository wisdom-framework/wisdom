package org.wisdom.api.crypto;

/**
 * Hash Algorithm.
 */
public enum Hash {

    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256"),
    SHA512("SHA-512");

    private String algorithm;

    Hash(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String toString() {
        return this.algorithm;
    }
}
