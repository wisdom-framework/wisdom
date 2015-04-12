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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates application secret.
 */
public final class ApplicationSecretGenerator {

    private ApplicationSecretGenerator() {
        // Avoid direct instantiation.
    }

    /**
     * Generates a new application key.
     * <p>
     * <strong>Note:</strong> the generation is attempted several times if previous generation did not generate 64
     * printable characters. Potentially, the generation may never ends, well, you would be really unlucky in that
     * case.
     * </p>
     *
     * @return the generated key.
     */
    public static String generate() {
        Random random = new SecureRandom();
        StringBuilder secret = new StringBuilder();
        for (int i = 1; i <= 64; i++) {
            secret.append((char) (random.nextInt(74) + 48));
        }
        String r = secret.toString().replaceAll("\\\\+", "/");
        if (r.length() != 64) {
            // It may happen if a non printable character is generated.
            return generate();
        }
        return r;
    }

    public static Pattern OLD_SECRET_LINE_PATTERN = Pattern.compile("application\\.secret=(.*)");
    public static Pattern SECRET_LINE_PATTERN = Pattern.compile("(\\p{Blank})*secret =(.*)");

    /**
     * Checks whether the application configuration file as the application secret.
     * If not generates one.
     *
     * @param project the Maven Project
     * @param log     the logger
     * @throws java.io.IOException if the application file cannot be read, or rewritten
     */
    public static void ensureOrGenerateSecret(MavenProject project, Log log) throws IOException {
        File conf = new File(project.getBasedir(), "src/main/configuration/application.conf");
        if (conf.isFile()) {
            List<String> lines = FileUtils.readLines(conf);

            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher matcher = OLD_SECRET_LINE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    if (matcher.group(1).length() == 0) {
                        lines.set(i, "application.secret=\"" + generate() + "\"");
                        changed = true;
                    }
                } else {
                    matcher = SECRET_LINE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        if (matcher.group(2).trim().length() == 0) {
                            lines.set(i, "  secret = \"" + generate() + "\"");
                            changed = true;
                        }
                    }
                }

            }

            if (changed) {
                FileUtils.writeLines(conf, lines);
                log.info("Application Secret generated - the configuration file was updated.");
            }

        }
    }

    /**
     * Generates a fake configuration file only containing a fake application secret.
     * This mode must not be used in production.
     *
     * @param output the configuration file (parent directories must exist)
     * @throws IOException if the file cannot be written
     */
    public static void generateFakeConfiguration(File output) throws IOException {
        FileUtils.write(output, "# Fake Application Configuration - Do not use this file in production \n" +
                "application.secret=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    }
}
