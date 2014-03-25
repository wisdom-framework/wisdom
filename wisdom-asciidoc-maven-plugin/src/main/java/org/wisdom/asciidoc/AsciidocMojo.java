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
package org.wisdom.asciidoc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.asciidoctor.*;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Transform Asciidoc files.
 */
@Mojo(name = "compile-asciidoc", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class AsciidocMojo extends AbstractWisdomWatcherMojo implements Constants {


    @Parameter(property = Options.ATTRIBUTES, required = false)
    protected Map<String, Object> attributes = new HashMap<String, Object>();
    @Parameter(property = Options.BACKEND, defaultValue = "html5", required = true)
    protected String backend = "";
    @Parameter(property = Options.COMPACT, required = false)
    protected boolean compact = false;
    @Parameter(property = Options.DOCTYPE, defaultValue = "article", required = true)
    protected String doctype;
    @Parameter(property = Options.ERUBY, required = false)
    protected String eruby = "";
    @Parameter(property = "headerFooter", required = false)
    protected boolean headerFooter = true;
    @Parameter(property = "templateDir", required = false)
    protected File templateDir;
    @Parameter(property = "templateEngine", required = false)
    protected String templateEngine;
    @Parameter(property = "imagesDir", required = false)
    protected String imagesDir = "images"; // use a string because otherwise html doc uses absolute path
    @Parameter(property = "sourceHighlighter", required = false)
    protected String sourceHighlighter = "";
    @Parameter(property = "extensions")
    protected List<String> extensions = new ArrayList<String>();
    @Parameter(property = "embedAssets")
    protected boolean embedAssets = false;
    @Parameter
    protected String stylesheet;
    @Parameter
    protected String stylesheetDir;
    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;
    private Asciidoctor instance;
    @Component
    private MavenFileFilter mavenFileFilter;

    public void execute()
            throws MojoExecutionException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes/assets");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        if (instance == null) {
            instance = getAsciidoctorInstance();
        }

        final OptionsBuilder optionsBuilderExternals = OptionsBuilder.options().compact(compact)
                .safe(SafeMode.UNSAFE).eruby(eruby).backend(backend).docType(doctype).headerFooter(headerFooter)
                .inPlace(true);

        final OptionsBuilder optionsBuilderInternals = OptionsBuilder.options().compact(compact)
                .safe(SafeMode.UNSAFE).eruby(eruby).backend(backend).docType(doctype).headerFooter(headerFooter).inPlace(true);

        if (templateEngine != null) {
            optionsBuilderExternals.templateEngine(templateEngine);
            optionsBuilderInternals.templateEngine(templateEngine);
        }

        if (templateDir != null) {
            optionsBuilderExternals.templateDir(templateDir);
            optionsBuilderInternals.templateDir(templateDir);
        }

        if (sourceHighlighter != null) {
            attributes.put("source-highlighter", sourceHighlighter);
        }

        if (embedAssets) {
            attributes.put("linkcss!", true);
            attributes.put("data-uri", true);
        }

        if (imagesDir != null) {
            attributes.put("imagesdir", imagesDir);
        }

        if (stylesheet != null) {
            attributes.put(Attributes.STYLESHEET_NAME, stylesheet);
            attributes.put(Attributes.LINK_CSS, true);
        }

        if (stylesheetDir != null) {
            attributes.put(Attributes.STYLES_DIR, stylesheetDir);
        }

        optionsBuilderExternals.attributes(attributes);
        optionsBuilderInternals.attributes(attributes);

        try {
            for (final File f : scanSourceFiles(externalSources)) {
                renderFile(optionsBuilderExternals.asMap(), f);
            }
            for (final File f : scanSourceFiles(internalSources)) {
                renderFile(optionsBuilderInternals.asMap(), f);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error while compiling AsciiDoc file", e);
        }

    }

    protected Asciidoctor getAsciidoctorInstance() throws MojoExecutionException {
        return Asciidoctor.Factory.create();
    }

    private List<File> scanSourceFiles(File root) {
        final List<File> files;
        if (extensions == null || extensions.isEmpty()) {
            final DirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(root.getAbsolutePath());
            files = directoryWalker.scan();
        } else {
            final DirectoryWalker directoryWalker = new CustomExtensionDirectoryWalker(root.getAbsolutePath(), extensions);
            files = directoryWalker.scan();
        }
        return files;
    }

    protected void renderFile(Map<String, Object> options, File f) throws IOException {
        getLog().info("Compiling Asciidoc file >> " + f.getName());
        File filtered;
        if (FilenameUtils.directoryContains(internalSources.getCanonicalPath(), f.getCanonicalPath())) {
            filtered = findFileInDirectory(f, destinationForInternals);
        } else {
            filtered = findFileInDirectory(f, destinationForExternals);
        }
        if (filtered == null) {
            getLog().error("Cannot find the filtered version of " + f.getAbsolutePath() + ", " +
                    "using unprocessed file.");
            filtered = f;
        }
        instance.renderFile(filtered, options);
    }

    /**
     * Searches for a file with the same name as the given file in the given directory.
     *
     * @param file      the file
     * @param directory the directory
     * @return the found file or {@code null} if not found
     */
    private File findFileInDirectory(File file, File directory) {
        Collection<File> files = FileUtils.listFiles(directory, new NameFileFilter(file.getName()),
                TrueFileFilter.INSTANCE);

        if (files.isEmpty()) {
            return null;
        } else if (files.size() > 1) {
            getLog().warn("Finding several (filtered) candidates for file " + file.getName()
                    + " in " + directory.getAbsolutePath() + " : " + files);
        }

        return files.iterator().next();
    }

    @Override
    public boolean accept(File file) {
        if (extensions == null || extensions.isEmpty()) {
            return file.getName().endsWith(".ad") || file.getName().endsWith(".adoc") ||
                    file.getName().endsWith(".asciidoc");
        } else {
            for (String ext : extensions) {
                if (file.getName().endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }

    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            execute();
        } catch (MojoExecutionException e) {
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
        // TODO Fix this, by deleting the previously generated files.
        return fileCreated(file);
    }
}
