package org.wisdom.maven.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generate application secret.
 */
public class ApplicationSecretGenerator {

    public static String generate() {
        Random random = new SecureRandom();
        StringBuilder secret = new StringBuilder();
        for (int i = 1; i <= 64; i++) {
            secret.append((char)(random.nextInt(74) + 48));
        }
        String r = secret.toString().replaceAll("\\\\+", "/");
        if (r.length() != 64) {
            // It may happen if a non printable character is generated.
            return generate();
        }
        return r;
    }
}
