package org.ow2.chameleon.wisdom.maven.utils;

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
        return secret.toString().replaceAll("\\\\+", "/");
    }
}
