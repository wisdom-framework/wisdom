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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.BuildConstants;
import org.wisdom.maven.utils.DefaultMaven2OsgiConverter;
import org.wisdom.maven.utils.PlexusLoggerWrapper;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * This mojo is responsible for the creation of the webjar containing the internal asset of the project. The created
 * webjar is conform to the webjar specification.
 * <p>
 * The webjar artifact is generated using the "webjar" classifier.
 * <p>
 * In watch mode, only the webjar is re-created on every resource change.
 */
@Mojo(name = "package-webjar", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PACKAGE)
public class WebJarPackager extends AbstractWisdomWatcherMojo {

    /**
     * The root of the webjar in the jar file.
     */
    protected static final String ROOT = "META-INF/resources/webjars/";

    /**
     * WebJar configuration. Let you define the file set to include, the webjar name (it uses the artifact id by
     * default), the webjar version (it uses the project's version by default), and the classifier.
     */
    @Parameter
    WebJar webjar;

    /**
     * Flag to enabled or disabled the webjar packaging. By default it's disabled.
     */
    @Parameter(defaultValue = "false")
    boolean packageWebJar;

    @Override
    public void execute() throws MojoExecutionException {
        if (!enabled()) {
            removeFromWatching();
            return;
        }

        FileSet set = new FileSet();
        set.setDirectory(getInternalAssetOutputDirectory().getAbsolutePath());
        if (webjar == null) {
            webjar = new WebJar(project.getArtifactId(), project.getVersion(), "webjar", set);
        } else {
            if (webjar.getFileset() == null) {
                webjar.setFileset(set);
            }
            else if(webjar.getFileset().getDirectory() == null){
                getLog().info("No directory define in the webjar fileset - use the AssetOutputDirectory");
                webjar.getFileset().setDirectory(getInternalAssetOutputDirectory().getAbsolutePath());
            }
            if (webjar.getName() == null) {
                webjar.setName(project.getArtifactId());
            }
            if (webjar.getVersion() == null) {
                webjar.setVersion(project.getVersion());
            }
            if (webjar.getClassifier() == null) {
                webjar.setClassifier("webjar");
            }
        }

        try {
            File out = process();
            if (out != null) {
                projectHelper.attachArtifact(project, out, webjar.getClassifier());

                // Copy the build file to the application directory.
                // The application bundle uses the Wisdom convention (bundle symbolic name - version.jar
                File dest = new File(new File(getWisdomRootDirectory(), "application"),  webjar.getOutputFileName());

                // Write a small notice about the copy
                getLog().info("Copying " + dest.getName() + " to " + dest.getAbsolutePath());
                FileUtils.copyFile(out, dest, true);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failure while building the webjar", e);
        }
    }

    private File process() throws IOException, ManifestException {
        getLog().info("Building webjar for " + project.getArtifactId());
        File output = new File(buildDirectory, webjar.getOutputFileName());
        FileUtils.deleteQuietly(output);

        Collection<File> selected = webjar.getSelectedFiles();
        if (selected.isEmpty()) {
            getLog().warn("No file selected in the webjar - skipping creation");
            return null;
        }
        String root = computeRoot();

        FileUtils.deleteQuietly(output);

        JarArchiver archiver = new JarArchiver();
        archiver.enableLogging(new PlexusLoggerWrapper(getLog()));
        String base = webjar.getFileset().getDirectory();
        for (File file : selected) {
            final String destFileName = root + "/" + file.getAbsolutePath().substring(base.length() + 1);
            getLog().debug(file.getName() + " => " + destFileName);
            archiver.addFile(file, destFileName);
        }
        Manifest manifest = Manifest.getDefaultManifest();
        manifest.getMainSection().addConfiguredAttribute(new Manifest.Attribute("Webjar-Name", webjar.getName()));
        manifest.getMainSection().addConfiguredAttribute(new Manifest.Attribute("Webjar-Version", webjar.getVersion()));
        manifest.getMainSection().addConfiguredAttribute(new Manifest.Attribute("Created-By", "Wisdom Framework " +
                BuildConstants.get("WISDOM_PLUGIN_VERSION")));
        archiver.addConfiguredManifest(manifest);
        archiver.setDestFile(output);
        archiver.createArchive();
        return output;
    }

    private String computeRoot() {
        return ROOT + webjar.getName() + "/" + webjar.getVersion();
    }

    protected boolean enabled() {
        return packageWebJar || webjar != null;
    }

    /**
     * Checks whether the given file is managed by the current watcher. Notice that implementation must not check
     * for the existence of the file as this method is also called for deleted files.
     *
     * @param file is the file.
     * @return {@literal true} if the watcher is interested in being notified on an event
     * attached to the given file,
     * {@literal false} otherwise.
     */
    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, getInternalAssetsDirectory());
    }

    /**
     * Notifies the watcher that a new file is created.
     *
     * @param file is the file.
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * return {@literal true} to let other watchers be notified.
     * @throws org.wisdom.maven.WatchingException if the watcher failed to process the given file.
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            process();
        } catch (Exception e) {
            throw new WatchingException("Failure while building the webjar", e);
        }
        return true;
    }

    /**
     * Notifies the watcher that a file has been modified.
     *
     * @param file is the file.
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * returns {@literal true} to let other watchers to be notified.
     * @throws org.wisdom.maven.WatchingException if the watcher failed to process the given file.
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * Notifies the watcher that a file was deleted.
     *
     * @param file the file
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * return {@literal true} to let other watchers be notified.
     * @throws org.wisdom.maven.WatchingException if the watcher failed to process the given file.
     */
    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        return fileCreated(file);
    }

}
