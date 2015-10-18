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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wisdom.maven.node.NPM.npm;

/**
 * Compiles CoffeeScript files to JavaScript.
 * All '.coffee' files from 'src/main/resources/assets' are compiled to 'target/classes/',
 * while 'src/main/assets/' are compiled to 'target/wisdom/assets'.
 */
@Mojo(name = "compile-coffeescript", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class CoffeeScriptCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {

    public static final String COFFEE_SCRIPT_NPM_NAME = "coffee-script";
    public static final String COFFEE_SCRIPT_COMMAND = "coffee";
    public static final String ERROR_TITLE = "CoffeeScript Compilation Error";
    public static final String COFFEESCRIPT_VERSION = "1.10.0";

    private NPM coffee;

    /**
     * The CoffeeScript version.
     * It must be a version available from the NPM registry
     *
     * @see <a href="https://www.npmjs.org/">NPM Web Site</a>.
     */
    @Parameter(defaultValue = COFFEESCRIPT_VERSION)
    String coffeeScriptVersion;

    /**
     * Finds and compiles coffeescript files.
     *
     * @throws MojoExecutionException if the compilation failed.
     */
    @Override
    public void execute() throws MojoExecutionException {


        coffee = npm(this, COFFEE_SCRIPT_NPM_NAME, coffeeScriptVersion);

        try {
            if (getInternalAssetsDirectory().isDirectory()) {
                getLog().info("Compiling CoffeeScript files from " + getInternalAssetsDirectory().getAbsolutePath());
                invokeCoffeeScriptCompiler(getInternalAssetsDirectory(), getInternalAssetOutputDirectory());
            }

            if (getExternalAssetsDirectory().isDirectory()) {
                getLog().info("Compiling CoffeeScript files from " + getExternalAssetsDirectory().getAbsolutePath());
                invokeCoffeeScriptCompiler(getExternalAssetsDirectory(), getExternalAssetsOutputDirectory());
            }
        } catch (WatchingException e) {
            throw new MojoExecutionException("Error during the CoffeeScript compilation", e);
        }
    }

    /**
     * Accepts all `coffee` files that are in 'src/main/resources/assets' or in 'src/main/assets/'.
     *
     * @param file the file
     * @return {@literal true} if the file is accepted.
     */
    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "coffee");
    }


    private void compile(File file) throws WatchingException {
        if (file == null) {
            return;
        }
        File out = getOutputFile(file, "js");
        getLog().info("Compiling CoffeeScript " + file.getAbsolutePath() + " to " + out.getAbsolutePath());

        invokeCoffeeScriptCompiler(file, out.getParentFile());
    }

    private void invokeCoffeeScriptCompiler(File input, File out) throws WatchingException {
        try {
            int exit = coffee.execute(COFFEE_SCRIPT_COMMAND, "--compile", "--map", "--output", out.getAbsolutePath(),
                    input.getAbsolutePath());
            getLog().debug("CoffeeScript compilation exits with " + exit + " status");
        } catch (MojoExecutionException e) {
            if (!Strings.isNullOrEmpty(coffee.getLastErrorStream())) {
                throw build(coffee.getLastErrorStream(), input);
            } else {
                throw new WatchingException(ERROR_TITLE, "Error while compiling " + input
                        .getAbsolutePath(), input, e);
            }
        }
    }

    public static Pattern COFFEE_COMPILATION_ERROR = Pattern.compile("(.*):([0-9]*):([0-9]*):(.*)");

    public WatchingException build(String message, File source) {
        String[] lines = message.split("\n");
        for (String l : lines) {
            if (!Strings.isNullOrEmpty(l)) {
                message = l.trim();
                break;
            }
        }
        final Matcher matcher = COFFEE_COMPILATION_ERROR.matcher(message);
        if (matcher.matches()) {
            String path = matcher.group(1);
            String line = matcher.group(2);
            String character = matcher.group(3);
            String reason = matcher.group(4);
            File file = new File(path);
            return new WatchingException(ERROR_TITLE, reason, file,
                    Integer.valueOf(line), Integer.valueOf(character), null);
        } else {
            return new WatchingException(ERROR_TITLE, message, source, null);
        }
    }

    /**
     * A file is created - compiles it.
     *
     * @param file the file
     * @return {@literal true}
     * @throws WatchingException if the compilation failed
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        compile(file);
        return true;
    }

    /**
     * A file is updated - compiles it.
     *
     * @param file the file
     * @return {@literal true}
     * @throws WatchingException if the compilation failed
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        compile(file);
        return true;
    }

    /**
     * A file is deleted - delete the output files (".js" and ".map").
     *
     * @param file the file
     * @return {@literal true}
     */
    @Override
    public boolean fileDeleted(File file) {
        FileUtils.deleteQuietly(getOutputFile(file, "js"));
        FileUtils.deleteQuietly(getOutputFile(file, "js.map"));
        // Keep the deletion of .map file for coffeescript < 1.8.0
        FileUtils.deleteQuietly(getOutputFile(file, "map"));
        return true;
    }

}
