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

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.asciidoctor.*;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.Asciidoctor.Factory.create;



/**
 * Compiles Asciidoc files to HTML documents.
 */
@Mojo(name = "compile-asciidoc", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class AsciidocMojo extends AbstractWisdomWatcherMojo implements Constants {

    @Parameter(property = Options.ATTRIBUTES, required = false)
    protected Map<String, Object> attributes = new HashMap<>();
    @Parameter(property = Options.BACKEND, defaultValue = "html5", required = true)
    protected String backend = "";
    @Parameter(property = Options.COMPACT, required = false)
    protected boolean compact = false;
    @Parameter(property = Options.DOCTYPE, defaultValue = "article", required = true)
    protected String doctype;
    @Parameter(property = Options.ERUBY, required = false, defaultValue = "erb")
    protected String eruby = "erb";
    @Parameter(property = "headerFooter", required = false, defaultValue = "true")
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
    protected List<String> extensions = new ArrayList<>();
    @Parameter(property = "embedAssets")
    protected boolean embedAssets = false;
    @Parameter
    protected String stylesheet;
    @Parameter
    protected String stylesheetDir;

    /**
     * List of ant-style patterns used to specify the asciidoc file that should be included when compiling.
     */
    @Parameter
    protected String[] includes;

    /**
     * List of ant-style patterns used to specify the asciidoc file that should **NOT** be included when
     * compiling.
     */
    protected String[] excludes;

    Asciidoctor instance;

    /**
     * Enable or disable the watch mode, enabled by default.
     */
    @Parameter(defaultValue = "true")
    private boolean watch;

    /**
     * Compiles Asciidoc files from the internal and external assets to HTML.
     *
     * @throws MojoExecutionException if the processing failed
     */
    public void execute()
            throws MojoExecutionException {

        if (! watch) {
            removeFromWatching();
        }

        if (extensions == null || extensions.isEmpty()) {
            extensions = ImmutableList.of("ad", "asciidoc", "adoc");
        }

        if (instance == null) {
            instance = getAsciidoctorInstance();
        }

        final OptionsBuilder optionsBuilderExternals = OptionsBuilder.options().compact(compact)
                .safe(SafeMode.UNSAFE).eruby(eruby).backend(backend).docType(doctype).headerFooter(headerFooter).inPlace(true);

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

        IOFileFilter filter = null;
        if (includes != null && includes.length != 0) {
            filter = new WildcardFileFilter(includes);
            if (excludes != null && excludes.length != 0) {
                filter = new AndFileFilter(filter, new NotFileFilter(new WildcardFileFilter(excludes)));
            }
        }
        try {
            for (File file : getResources(extensions)) {
                if (filter == null || filter.accept(file)) {
                    renderFile(optionsBuilderExternals.asMap(), file);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error while compiling AsciiDoc file", e);
        }

    }

    protected Asciidoctor getAsciidoctorInstance() throws MojoExecutionException {
        return create(this.getClass().getClassLoader());
    }


    protected void renderFile(Map<String, Object> options, File f) throws IOException {
        File filtered = getFilteredVersion(f);
        boolean unfiltered;

        if (filtered == null) {
            // It was not copied.
            getLog().error("Cannot find the filtered version of " + f.getAbsolutePath() + ", " +
                    "using unprocessed file.");
            filtered = f;
            unfiltered = true;
        } else {
            // It was copied.
            unfiltered = false;
        }

        instance.renderFile(filtered, options);

        // Move the file to the expected place if not filtered
        if (unfiltered) {
            String name = filtered.getName().substring(0, filtered.getName().lastIndexOf(".")) + ".html";
            File output = new File(filtered.getParentFile(), name);
            if (output.isFile()) {
                // Move...
                File finalFile = getOutputFile(filtered, "html");
                FileUtils.moveFile(output, finalFile);
            } else {
                getLog().error("Cannot find the output file for " + filtered.getAbsolutePath());
            }
        }
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.hasExtension(file, extensions);
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
        File output = getOutputFile(file, "html");
        FileUtils.deleteQuietly(output);
        return fileCreated(file);
    }
}
