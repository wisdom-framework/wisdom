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
package org.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.BundlePackager;
import org.wisdom.maven.utils.PlexusLoggerWrapper;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;

/**
 * The mojo packaging the wisdom application.
 */
@Mojo(name = "package", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PACKAGE)
public class BundlePackagerMojo extends AbstractWisdomWatcherMojo implements Constants {

    /**
     * If set to {@literal true} disables the construction of the zip file containing the Wisdom distribution.
     * This option is useful for components and services that are not 'complete' application just one brick that is
     * used in another application.
     */
    @Parameter(defaultValue = "false")
    private boolean disableDistributionPackaging;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            createApplicationBundle();
            if (! disableDistributionPackaging) {
                createApplicationDistribution();
            } else {
                getLog().debug("Creation of the zip file disabled");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot build wisdom application", e);
        }
    }

    private void createApplicationBundle() throws Exception {
        File finalFile = new File(this.buildDirectory, this.project.getArtifactId() + "-" + this.project
                .getVersion() + ".jar");
        BundlePackager.bundle(this.basedir, finalFile);

        // Declare the bundle as main project artifact.
        Artifact mainArtifact = project.getArtifact();
        mainArtifact.setFile(finalFile);

        // Copy the build file to the application directory.
        FileUtils.copyFileToDirectory(finalFile, new File(getWisdomRootDirectory(), "application"), true);
    }

    private void createApplicationDistribution() throws IOException {
        File distFile = new File(this.buildDirectory, this.project.getArtifactId() + "-" + this.project
                .getVersion() + ".zip");
        ZipArchiver archiver = new ZipArchiver();
        archiver.enableLogging(new PlexusLoggerWrapper(getLog()));
        archiver.addDirectory(getWisdomRootDirectory());
        archiver.setDestFile(distFile);
        archiver.createArchive();

        projectHelper.attachArtifact(project, "zip", distFile);
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getJavaSource(basedir))
                || WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            createApplicationBundle();
        } catch (Exception e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        return fileCreated(file);
    }
}
