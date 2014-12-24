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
package org.wisdom.maven.tools;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.wisdom.maven.utils.Properties2HoconConverter;

import java.io.File;
import java.io.IOException;

/**
 * A Mojo converting the application.conf file to the "hocon".
 */
@Mojo(requiresProject = false,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        name= "properties2hocon")
public class HoconConverterMojo extends AbstractMojo {

        @Parameter(defaultValue = "${properties}")
        File properties;

        @Parameter(defaultValue = "${backup}")
        boolean backup = true;

        /**
         * The project base directory.
         */
        @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
        public File basedir;

        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {
                if (properties == null) {
                        properties = new File(basedir, "src/main/configuration/application.conf");
                }

                if (! properties.exists()) {
                        throw new MojoExecutionException("Cannot convert '" + properties.getAbsolutePath() + "' to " +
                                "hocon - the file does not exist");
                }

                try {
                        File output = Properties2HoconConverter.convert(properties, backup);
                        getLog().info("The properties file '" + properties.getName() + "' has been converted to the " +
                                "hocon syntax (" + output.getAbsolutePath() + ") - please review before using");
                        if (backup) {
                                getLog().info("A backup file has been created : "
                                        + properties.getAbsolutePath() + ".backup");
                        }
                } catch (IOException e) {
                        throw new MojoExecutionException("The conversion to '" + properties.getAbsolutePath() + "' " +
                                "has failed", e);
                }
        }
}
