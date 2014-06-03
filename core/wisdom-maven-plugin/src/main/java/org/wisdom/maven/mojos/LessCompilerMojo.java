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

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wisdom.maven.node.NPM.npm;

/**
 * Compiles less files.
 */
@Mojo(name = "compile-less", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class LessCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {

    public static final String LESS_NPM_NAME = "less";
    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;
    private NPM less;

    private static final Pattern LESS_ERROR_PATTERN =
            Pattern.compile("\\[31m(.*)\\[39m\\[31m in .* on line ([0-9]*), column ([0-9]*):.*");

    /**
     * The Less version.
     * It must be a version available from the NPM registry
     *
     * @see <a href="https://www.npmjs.org/">NPM Web Site</a>.
     */
    @Parameter(defaultValue = "1.7.0")
    String lessVersion;

    @Override
    public void execute() throws MojoExecutionException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        less = npm(this, LESS_NPM_NAME, lessVersion);

        try {
            if (internalSources.isDirectory()) {
                getLog().info("Compiling less files from " + internalSources.getAbsolutePath());
                Collection<File> files = FileUtils.listFiles(internalSources, new String[]{"less"}, true);
                for (File file : files) {
                    if (file.isFile()) {
                        compile(file);
                    }
                }
            }

            if (externalSources.isDirectory()) {
                getLog().info("Compiling less files from " + externalSources.getAbsolutePath());
                Collection<File> files = FileUtils.listFiles(externalSources, new String[]{"less"}, true);
                for (File file : files) {
                    if (file.isFile()) {
                        compile(file);
                    }
                }
            }
        } catch (WatchingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "less");
    }

    private File getOutputCSSFile(File input) {
        File source;
        File destination;
        if (input.getAbsolutePath().startsWith(internalSources.getAbsolutePath())) {
            source = internalSources;
            destination = destinationForInternals;
        } else if (input.getAbsolutePath().startsWith(externalSources.getAbsolutePath())) {
            source = externalSources;
            destination = destinationForExternals;
        } else {
            return null;
        }

        String cssFileName = input.getName().substring(0, input.getName().length() - ".less".length()) + ".css";
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + cssFileName);
    }

    public void compile(File file) throws WatchingException {
        File out = getOutputCSSFile(file);
        getLog().info("Compiling " + file.getAbsolutePath() + " to " + out.getAbsolutePath());
        try {
            int exit = less.execute("lessc", file.getAbsolutePath(), out.getAbsolutePath());
            getLog().debug("Less execution exiting with status " + exit);
        } catch (MojoExecutionException e) { //NOSONAR
            throw buildWatchingException(less.getLastErrorStream(), file, e);
        }

        if (!out.isFile()) {
            throw new WatchingException("Error during the compilation of " + file.getAbsoluteFile() + ", check log");
        }
    }

    private WatchingException buildWatchingException(String stream, File file, MojoExecutionException e) {
        String[] lines = stream.split("\n");
        for (String l : lines) {
            if (!Strings.isNullOrEmpty(l)) {
                stream = l.trim();
                break;
            }
        }
        final Matcher matcher = LESS_ERROR_PATTERN.matcher(stream);
        if (matcher.matches()) {
            String line = matcher.group(2);
            String character = matcher.group(3);
            String reason = matcher.group(1);
            return new WatchingException("Less Compilation Error: " + reason, file,
                    Integer.valueOf(line), Integer.valueOf(character), null);
        } else {
            return new WatchingException("Less Compilation Error : " + stream, e.getCause());
        }
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        compile(file);
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        compile(file);
        return true;
    }

    @Override
    public boolean fileDeleted(File file) {
        File theFile = getOutputCSSFile(file);
        FileUtils.deleteQuietly(theFile);
        return true;
    }

}
