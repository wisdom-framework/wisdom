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
import com.google.common.collect.ImmutableList;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public static final String ERROR_TITLE = "Less Compilation Error";
    public static final String LESS_VERSION = "1.7.5";

    private NPM less;

    private static final Pattern LESS_ERROR_PATTERN =
            Pattern.compile("\\[31m(.*)\\[39m\\[31m in .* on line ([0-9]*), column ([0-9]*):.*");

    /**
     * The Less version.
     * It must be a version available from the NPM registry
     *
     * @see <a href="https://www.npmjs.org/">NPM Web Site</a>.
     */
    @Parameter(defaultValue = LESS_VERSION)
    String lessVersion;

    /**
     * Less compiler argument.
     * Check <a href="http://lesscss.org/usage/index.html#command-line-usage">Less Command Line Usage</a> for further
     * details.
     */
    @Parameter
    String lessArguments;

    @Override
    public void execute() throws MojoExecutionException {
        less = npm(this, LESS_NPM_NAME, lessVersion);

        try {
            for (File file : getResources(ImmutableList.of("less"))) {
                if(!file.getName().startsWith("_")){
                    compile(file);
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

    public void compile(File file) throws WatchingException {
        File out = getOutputFile(file, "css");
        getLog().info("Compiling " + file.getAbsolutePath() + " to " + out.getAbsolutePath());
        try {
            int exit = less.execute("lessc", getCommandLineArguments(file.getAbsolutePath(), out.getAbsolutePath()));
            getLog().debug("Less execution exiting with status " + exit);
        } catch (MojoExecutionException e) { //NOSONAR
            throw buildWatchingException(less.getLastErrorStream(), file, e);
        }

        if (!out.isFile()) {
            throw new WatchingException(ERROR_TITLE, "Error during the compilation of " + file
                    .getAbsoluteFile() + "," + " check log", file, null);
        }
    }

    private String[] getCommandLineArguments(String in, String out) {
        List<String> params = new ArrayList<>();
        if (lessArguments != null) {
            params.addAll(Arrays.asList(lessArguments.split(" ")));
        }
        params.add(in);
        params.add(out);
        return params.toArray(new String[params.size()]);
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
            return new WatchingException("Less Compilation Error", reason, file,
                    Integer.valueOf(line), Integer.valueOf(character), null);
        } else {
            return new WatchingException("Less Compilation Error", stream, file, e.getCause());
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
        File theFile = getOutputFile(file, "css");
        FileUtils.deleteQuietly(theFile);
        return true;
    }

}
