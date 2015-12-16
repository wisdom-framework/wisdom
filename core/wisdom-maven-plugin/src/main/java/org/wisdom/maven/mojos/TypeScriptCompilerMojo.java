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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wisdom.maven.node.NPM.npm;

/**
 * Compiles TypeScript files to JavaScript.
 * All '.ts' files from 'src/main/resources/assets' are compiled to 'target/classes/',
 * while 'src/main/assets/' are compiled to 'target/wisdom/assets'.
 */
@Mojo(name = "compile-typescript", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class TypeScriptCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {

    /**
     * The typescript NPM name.
     */
    public static final String TYPE_SCRIPT_NPM_NAME = "typescript";

    /**
     * The command to be launched.
     */
    public static final String TYPE_SCRIPT_COMMAND = "tsc";

    /**
     * The title used for TypeScript compilation error.
     */
    public static final String ERROR_TITLE = "TypeScript Compilation Error";

    /**
     * The regex used to extract information from typescript error message.
     */
    private static final Pattern TYPESCRIPT_COMPILATION_ERROR = Pattern.compile("(.*)\\(([0-9]*),([0-9]*)\\):(.*)");
    public static final String EXTENSION = "ts";


    private NPM npm;

    /**
     * Configuration of the typescript processing.
     */
    @Parameter
    TypeScript typescript;

    private File internalSources;
    private File destinationForInternals;

    private File externalSources;
    private File destinationForExternals;

    /**
     * Finds and compiles coffeescript files.
     *
     * @throws MojoExecutionException if the compilation failed.
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (typescript == null) {
            typescript = new TypeScript();
        }

        if (!typescript.isEnabled()) {
            removeFromWatching();
            getLog().info("Typescript processing disabled");
            return;
        }

        npm = npm(this, TYPE_SCRIPT_NPM_NAME, typescript.getVersion());
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        try {
            if (internalSources.isDirectory()) {
                getLog().info("Compiling TypeScript files with 'tsc' from " + internalSources.getAbsolutePath());
                processDirectory(internalSources, destinationForInternals);
            }

            if (externalSources.isDirectory()) {
                getLog().info("Compiling TypeScript files with 'tsc' from " + externalSources.getAbsolutePath());
                processDirectory(externalSources, destinationForExternals);
            }
        } catch (WatchingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
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
                        && WatcherUtils.hasExtension(file, EXTENSION);
    }

    /**
     * A file is created - process it.
     *
     * @param file the file
     * @return {@literal true} as the pipeline should continue
     * @throws WatchingException if the processing failed
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        if (WatcherUtils.isInDirectory(file, internalSources)) {
            processDirectory(internalSources, destinationForInternals);
        } else if (WatcherUtils.isInDirectory(file, externalSources)) {
            processDirectory(externalSources, destinationForExternals);
        }
        return true;
    }

    /**
     * Process all typescripts file from the given directory. Output files are generated in the given destination.
     *
     * @param input       the input directory
     * @param destination the output directory
     * @throws WatchingException if the compilation failed
     */
    protected void processDirectory(File input, File destination) throws WatchingException {
        if (! input.isDirectory()) {
            return;
        }

        if (! destination.isDirectory()) {
            destination.mkdirs();
        }

        // Now execute the compiler
        // We compute the set of argument according to the Mojo's configuration.
        try {
            Collection<File> files = FileUtils.listFiles(input, new String[]{"ts"}, true);
            if (files.isEmpty()) {
                return;
            }

            List<String> arguments = typescript.createTypeScriptCompilerArgumentList(input, destination, files);
            getLog().info("Invoking the TypeScript compiler with " + arguments);
            npm.registerOutputStream(true);
            int exit = npm.execute(TYPE_SCRIPT_COMMAND,
                    arguments.toArray(new String[arguments.size()]));
            getLog().debug("TypeScript Compiler execution exiting with status: " + exit);
        } catch (MojoExecutionException e) {
            // If the NPM execution has caught an error stream, try to create the associated watching exception.
            if (!Strings.isNullOrEmpty(npm.getLastOutputStream())) {
                throw build(npm.getLastOutputStream());
            } else {
                throw new WatchingException(ERROR_TITLE, "Error while compiling " + input
                        .getAbsolutePath(), input, e);
            }
        }
    }

    /**
     * Creates the Watching Exception by parsing the NPM error log.
     *
     * @param message the log
     * @return the watching exception
     */
    private WatchingException build(String message) {
        String[] lines = message.split("\n");
        for (String l : lines) {
            if (!Strings.isNullOrEmpty(l)) {
                message = l.trim();
                break;
            }
        }
        final Matcher matcher = TYPESCRIPT_COMPILATION_ERROR.matcher(message);
        if (matcher.matches()) {
            String path = matcher.group(1);
            String line = matcher.group(2);
            String character = matcher.group(3);
            String reason = matcher.group(4);
            File file = new File(path);
            return new WatchingException(ERROR_TITLE, reason, file,
                    Integer.parseInt(line), Integer.parseInt(character), null);
        } else {
            return new WatchingException(ERROR_TITLE, message, null, null);
        }
    }

    /**
     * A file is updated - process it.
     *
     * @param file the file
     * @return {@literal true} as the pipeline should continue
     * @throws WatchingException if the processing failed
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * A file is deleted - delete the output.
     * This method deletes the generated js, js.map (source map) and declaration (d.ts) files.
     *
     * @param file the file
     * @return {@literal true} as the pipeline should continue
     */
    @Override
    public boolean fileDeleted(File file) {
        FileUtils.deleteQuietly(getOutputFile(file, "js"));
        FileUtils.deleteQuietly(getOutputFile(file, "js.map"));
        FileUtils.deleteQuietly(getOutputFile(file, "d.ts"));
        return true;
    }

    /**
     * Gets the output file for the given input and the given extension.
     *
     * @param input the input file
     * @param ext   the extension
     * @return the output file, may not exist
     */
    public File getOutputFile(File input, String ext) {
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

        String jsFileName = input.getName().substring(0, input.getName().length() - ".ts".length()) + "." + ext;
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + jsFileName);

    }

}
