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

import com.google.javascript.jscomp.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.LoggedOutputStream;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Compiles and minifies JavaScript files.
 * <ul>
 * <ol>It compiles (checks) JavaScript files from src/main/resources/assets and /src/main/assets.</ol>
 * <ol>It minifies these JavaScript files</ol>
 * <ol>It minifies the JavaScript file generated from CoffeeScript</ol>
 * </ul>
 * <p/>
 * This mojo makes the assumption that the files are already copied/generated to their destination directory,
 * when it is executed.
 */
@Mojo(name = "compile-javascript", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class JavaScriptCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {

    /**
     * Selects the compilation level for Google Closure among SIMPLE_OPTIMIZATIONS,
     * WHITESPACE_ONLY and ADVANCED_OPTIMIZATIONS.
     * Be aware that ADVANCED_OPTIMIZATIONS modifies the API of your code.
     */
    @Parameter(defaultValue = "SIMPLE_OPTIMIZATIONS")
    public CompilationLevel googleClosureCompilationLevel;
    @Parameter(defaultValue = "false")
    public boolean googleClosurePrettyPrint;

    @Parameter(defaultValue = "${skipGoogleClosure}")
    public boolean skipGoogleClosure;

    /**
     * Minified file extension parameter, lets the user define their own extensions to use with
     * minification. Must not contain the {@literal .js} extension.
     */
    @Parameter(defaultValue = "-min")
    public String googleClosureMinifierSuffix;

    private File destinationForInternals;
    private File destinationForExternals;

    public static final String COMPILE_TITLE = "Compiling JavaScript files from";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipGoogleClosure) {
            getLog().debug("Skipping Google Closure Compilation");
            removeFromWatching();
            return;
        }

        this.destinationForInternals = new File(buildDirectory, "classes/assets");
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        try {
            if (destinationForInternals.isDirectory()) {
                getLog().info(COMPILE_TITLE + destinationForInternals.getAbsolutePath());
                compile(destinationForInternals);
            }

            if (destinationForExternals.isDirectory()) {
                getLog().info(COMPILE_TITLE + destinationForExternals.getAbsolutePath());
                compile(destinationForExternals);
            }
        } catch (WatchingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "js", "coffee")
                        && !isMinified(file)
                        && !isInLibs(file) ;
    }

    public static boolean isInLibs(File file) {
        return file.getAbsolutePath().contains("assets/libs/")  ||
                // On windows:
                file.getAbsolutePath().contains("assets\\libs\\");
    }

    public boolean isMinified(File file) {
        return file.getName().endsWith("min.js")
                || file.getName().endsWith(googleClosureMinifierSuffix + ".js");
    }

    public File getMinifiedFile(File file) {
        String name = file.getName().replace(".js", googleClosureMinifierSuffix + ".js");
        return new File(file.getParentFile(), name);
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        if (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir))) {
            compile(destinationForExternals);
        } else if (WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir))) {
            compile(destinationForInternals);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    @Override
    public boolean fileDeleted(File file) {
        if (!isMinified(file)) {
            File minified = getMinifiedFile(file);
            if (minified.isFile()) {
                minified.delete();
            }
        }
        return true;
    }

    private void compile(File base) throws WatchingException {
        getLog().info("Compressing JavaScript files from " + base.getName() + " using Google Closure");
        PrintStream out = new PrintStream(new LoggedOutputStream(getLog(), true), true);
        com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler(out);
        CompilerOptions options = newCompilerOptions();
        getLog().info("Compilation Level set to " + googleClosureCompilationLevel);
        googleClosureCompilationLevel.setOptionsForCompilationLevel(options);
        options.setPrettyPrint(googleClosurePrettyPrint);
        options.setPrintInputDelimiter(googleClosurePrettyPrint);
        // compilerOptions.setGenerateExports(generateExports);
        /*
         File sourceMapFile = new File(
                                JsarRelativeLocations
                                                .getCompileLocation(frameworkTargetDirectory),
                                compiledFilename + SOURCE_MAP_EXTENSION);

                if (generateSourceMap) {
                        attachSourceMapFileToOptions(sourceMapFile, compilerOptions);
                }
         */

        Collection<File> files = FileUtils.listFiles(base, new String[]{"js"}, true);
        List<File> store = new ArrayList<>();
        List<SourceFile> inputs = new ArrayList<>();
        //TODO Manage externs
        List<SourceFile> externs = new ArrayList<>();

        for (File file : files) {
            if (file.isFile() && !isMinified(file)  && ! isInLibs(file)) {
                store.add(file);
                inputs.add(SourceFile.fromFile(file));
            }
        }

        compiler.initOptions(options);
        final Result result = compiler.compile(externs, inputs, options);
        listErrors(result);

        if (!result.success) {
            throw new WatchingException("Error while compile JavaScript files, check log for more details");
        }

        String[] outputs = compiler.toSourceArray();
        for (int i = 0; i < store.size(); i++) {
            try {
                FileUtils.write(getMinifiedFile(store.get(i)), outputs[i]);
            } catch (IOException e) {
                throw new WatchingException("Cannot write minified JavaScript file : " + getMinifiedFile(store.get(i)), e);
            }
        }

    }

    /**
     * @return default {@link CompilerOptions} object to be used by compressor.
     */
    protected CompilerOptions newCompilerOptions() {
        final CompilerOptions options = new CompilerOptions();
        /**
         * According to John Lenz from the Closure Compiler project, if you are using the Compiler API directly, you
         * should specify a CodingConvention. {@link http://code.google.com/p/wro4j/issues/detail?id=155}
         */
        options.setCodingConvention(new ClosureCodingConvention());
        //set it to warning, otherwise compiler will fail
        options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES,
                CheckLevel.WARNING);
        return options;
    }

    /**
     * List the errors that google is providing from the compiler output.
     *
     * @param result the results from the compiler
     */
    private void listErrors(final Result result) {
        for (JSError warning : result.warnings) {
            getLog().warn(warning.toString());
        }

        for (JSError error : result.errors) {
            getLog().error(error.toString());
        }
    }

}
