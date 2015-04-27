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
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.wisdom.maven.ApplicationDistributionExtensions;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.osgi.BundlePackager;
import org.wisdom.maven.osgi.Reporter;
import org.wisdom.maven.utils.DefaultMaven2OsgiConverter;
import org.wisdom.maven.utils.PlexusLoggerWrapper;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;

/**
 * This mojo is responsible for the creation of the Wisdom application packages.
 * Wisdom distinguishes two packages: the jar file, which actually is an OSGi bundle,
 * and a zip file containing the whole server (including the jar file).
 * <p>
 * The jar file is an OSGi bundle containing your Java classes and internal resources (from src/main/resources). By
 * default, Wisdom packaging heuristics are applied, but you can customize the bundle packaging by providing the
 * 'src/main/osgi/osgi.bnd' file. This file contains the <a href="http://www.aqute.biz/Bnd/Bnd">BND</a> instructions. If
 * present heuristics are not used.
 * <p>
 * The zip file contains a distributable zip file containing the whole server (including your application).
 * <p>
 * In watch mode, only the jar files is re-created.
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

    /**
     * If set to {@literal false}, the distribution is packaged but not attached to the project. As a consequence it
     * will neither be installed in the local repository, nor deploy to remove repository.
     * <p>
     * If {@link #disableDistributionPackaging} is set to {@literal true}, this parameter is meaningless.
     */
    @Parameter(defaultValue = "true")
    private boolean attachDistribution;

    /**
     * If set to {@literal zip}, the distribution is packaged into a zip file.
     * <p>
     * If set to {@literal targz}, the distribution is packaged into a tar.gz file.
     * <p>
     * Currently supported : zip, targz
     */
    @Parameter(defaultValue = "zip")
    private ApplicationDistributionExtensions fileExtension;

    /**
     * Whether or not it should copy the created bundle to the wisdom directory in watch mode. Enabled by default. It
     * can be useful to disable it if the project is a webjar and the 'bundle' should not be deployed.
     */
    @Parameter(defaultValue = "true")
    boolean deployBundleToWisdom;

    /**
     * Execute method creates application bundle. Also creates the application distribution if the
     * {@link #wisdomDirectory} parameter is not set and if {@link #disableDistributionPackaging}
     * is set to false.
     *
     * @throws MojoExecutionException if the bundle or the distribution cannot be created
     *                                correctly, or if the resulting artifacts cannot be copied to their final destinations.
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            createApplicationBundle();
            if (!disableDistributionPackaging) {
                if (wisdomDirectory != null) {
                    getLog().warn("Cannot create the distribution of " + project.getArtifactId()
                            + " because it is using a remote Wisdom server (" + wisdomDirectory
                            .getAbsolutePath() + ").");
                } else {
                    createApplicationDistribution();
                }
            } else {
                getLog().debug("Creation of the zip file disabled");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot build wisdom application", e);
        }

        displayNonBundleLibraryWarning();
    }

    private void displayNonBundleLibraryWarning() {
        File libs = new File(getWisdomRootDirectory(), "libs");
        final String[] list = libs.list();
        if (libs.isDirectory() && list.length != 0) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("\n"
                    + "||==== WARNING ====\n");
            buffer.append(
                    "|| The current project contains non-bundle dependencies, \n" +
                            "|| these dependencies won't be copied in dependant project \n" +
                            "|| without being explicitly listed in the <libraries/> section of the pom.xml file:\n");
            for (String file : list) {
                buffer.append("|| * ").append(file).append("\n");
            }
            buffer.append("||=================");
            getLog().warn(buffer.toString());
        }
    }

    private void createApplicationBundle() throws IOException {
        File finalFile = new File(this.buildDirectory, this.project.getArtifactId() + "-" + this.project
                .getVersion() + ".jar");
        BundlePackager.bundle(this.basedir, finalFile, new Reporter() {
            @Override
            public void error(String msg) {
                getLog().error(msg);
            }

            @Override
            public void warn(String msg) {
                getLog().warn(msg);
            }
        });

        // Declare the bundle as main project artifact.
        Artifact mainArtifact = project.getArtifact();
        mainArtifact.setFile(finalFile);


        // Copy the build file to the application directory if enabled.
        if (deployBundleToWisdom) {
            deploy(finalFile);
        } else {
            getLog().info("Bundle not deployed to Wisdom - deployment disabled");
        }
    }

    private void deploy(File bundle) throws IOException {
        // The application bundle uses the Wisdom convention (bundle symbolic name - version.jar
        File applicationBundle = new File(new File(getWisdomRootDirectory(), APPLICATION_DIR),
                DefaultMaven2OsgiConverter.getBundleFileName(this.project));

        // Write a small notice about the copy
        getLog().info("Copying " + bundle.getName() + " to " + applicationBundle.getAbsolutePath());
        FileUtils.copyFile(bundle, applicationBundle, true);
    }

    private void createApplicationDistribution() throws IOException {
        File distFile = new File(this.buildDirectory, this.project.getArtifactId() + "-" + this.project
                .getVersion() + "." + fileExtension.extensionName);
        AbstractArchiver archiver;
        if (fileExtension == ApplicationDistributionExtensions.zip) {
            archiver = new ZipArchiver();
        } else {
            TarArchiver tarArchiver = new TarArchiver();
            tarArchiver.setCompression(TarArchiver.TarCompressionMethod.gzip);
            archiver = tarArchiver;
        }

        archiver.enableLogging(new PlexusLoggerWrapper(getLog()));
        archiver.addDirectory(getWisdomRootDirectory(), new String[0], new String[]{
                // Drop regular and test cache.
                "*-cache/**",
                "logs/**"});
        archiver.setDestFile(distFile);
        archiver.createArchive();

        if (attachDistribution) {
            projectHelper.attachArtifact(project, fileExtension.extensionName, distFile);
        }
    }

    /**
     * The bundle packaging has to be triggered when: a Java source file is modified,
     * an internal resource is modified or the `osgi.bnd` file (containing BND instructions) is modified.
     *
     * @param file the file
     * @return {@literal true} if an event on the given file should trigger the recreation of the bundle.
     */
    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getJavaSource(basedir))
                || WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir))
                || file.getAbsolutePath().equals(new File(basedir, INSTRUCTIONS_FILE).getAbsolutePath());
    }

    /**
     * On any change, we just repackage the bundle.
     *
     * @param file the created file.
     * @return {@literal true} as the pipeline must continue its execution.
     * @throws WatchingException if the bundle creation failed
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            createApplicationBundle();
        } catch (Exception e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    /**
     * On any change, we just repackage the bundle.
     *
     * @param file the updated file.
     * @return {@literal true} as the pipeline must continue its execution.
     * @throws WatchingException if the bundle creation failed
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * On any change, we just repackage the bundle.
     *
     * @param file the deleted file.
     * @return {@literal true} as the pipeline must continue its execution.
     * @throws WatchingException if the bundle creation failed
     */
    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        return fileCreated(file);
    }
}
