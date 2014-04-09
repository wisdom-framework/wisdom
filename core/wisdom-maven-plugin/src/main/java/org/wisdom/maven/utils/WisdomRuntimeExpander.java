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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.wisdom.maven.Constants;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;

/**
 * Downloads and Expands the Wisdom Runtime.
 */
public class WisdomRuntimeExpander {


    /**
     * Downloads and expands the Wisdom distribution.
     *
     * @param mojo           the mojo
     * @param destination    the output directory
     * @param useBaseRuntime whether or not to use the 'base runtime'. By default it should use the 'full runtime'
     *                       containing all technical services.
     * @return {@literal true} if the distribution has been downloaded and unpacked,
     * {@literal false} if the distribution was already present.
     * @throws MojoExecutionException if the distribution cannot be resolved.
     */
    public static boolean expand(AbstractWisdomMojo mojo, File destination,
                                 boolean useBaseRuntime) throws MojoExecutionException {
        if (destination.exists() && isWisdomAlreadyInstalled(destination)) {
            return false;
        }
        File archive;
        if (useBaseRuntime) {
            archive = DependencyFinder.resolve(mojo, mojo.plugin.getGroupId(),
                    Constants.WISDOM_BASE_RUNTIME_ARTIFACT_ID, mojo.plugin.getVersion(),
                    "zip");
        } else {
            archive = DependencyFinder.resolve(mojo, mojo.plugin.getGroupId(),
                    Constants.WISDOM_RUNTIME_ARTIFACT_ID, mojo.plugin.getVersion(),
                    "zip");
        }
        if (archive == null || !archive.exists()) {
            throw new MojoExecutionException("Cannot retrieve the Wisdom-Runtime file");
        }
        unzip(mojo, archive, destination);
        ensure(destination);
        return true;
    }

    private static boolean isWisdomAlreadyInstalled(File destination) {
        File bin = new File(destination, "bin");
        File core = new File(destination, "core");
        File runtime = new File(destination, "runtime");

        return bin.isDirectory() && core.isDirectory() && runtime.isDirectory();
    }

    private static void ensure(File destination) throws MojoExecutionException {
        if (!isWisdomAlreadyInstalled(destination)) {
            throw new MojoExecutionException("The installation of Wisdom in " + destination.getAbsolutePath() + " has" +
                    " failed");
        }
    }

    private static void unzip(final AbstractWisdomMojo mojo, File in, File out) {
        ZipUnArchiver unarchiver = new ZipUnArchiver(in);
        unarchiver.enableLogging(new PlexusLoggerWrapper(mojo.getLog()));
        unarchiver.setDestDirectory(out);
        unarchiver.extract();
    }


}
