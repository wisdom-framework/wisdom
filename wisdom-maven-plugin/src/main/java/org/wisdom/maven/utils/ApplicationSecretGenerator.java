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
package org.wisdom.maven.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generates application secret.
 */
public class ApplicationSecretGenerator {

    private ApplicationSecretGenerator() {
        // Avoid direct instantiation.
    }

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
