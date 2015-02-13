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
 * <p>
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
    @Parameter(defaultValue = "WHITESPACE_ONLY")
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

    @Parameter
    protected JavaScript javascript;

    private File destinationForInternals;
    private File destinationForExternals;

    public static final String COMPILE_TITLE = "Compiling JavaScript files from ";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipGoogleClosure) {
            getLog().debug("Skipping Google Closure Compilation");
            removeFromWatching();
            return;
        }

        this.destinationForInternals = new File(buildDirectory, "classes/assets");
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        // Check whether or not we have a custom configuration
        if (javascript == null) {
            getLog().info("No 'javascript' processing configuration, minifying all '.js' files individually");
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

        } else {
            try {
                compile(javascript);
            } catch (WatchingException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private void compile(JavaScript javaScript) throws WatchingException {
        if (javaScript.getAggregations() == null || javaScript.getAggregations().isEmpty()) {
            getLog().warn("No 'aggregation' configured in the 'javascript' processing configuration - skip " +
                    "processing");
            return;
        }

        if (javaScript.getExtern() != null  && ! javaScript.getExtern().isFile()) {
            throw new WatchingException("The 'extern' file " + javaScript.getExtern().getAbsolutePath() + " does not " +
                    "exist");
        }

        for (Aggregation aggregation : javaScript.getAggregations()) {
            compile(aggregation);
        }
    }

    private void compile(Aggregation aggregation) throws WatchingException {
        File output;
        if (aggregation.getOutput() == null) {
            output = getDefaultOutputFile(aggregation);
        } else {
            output = new File(aggregation.getOutput());
            output = fixPath(output);
        }

        if (!output.getParentFile().isDirectory()) {
            getLog().debug("Create directory " + output.getParentFile().getAbsolutePath() + " : "
                    + output.getParentFile().mkdirs());
        }

        getLog().info("Compressing JavaScript files from aggregation " + aggregation.getFiles() + " using Google Closure");
        PrintStream out = new PrintStream(new LoggedOutputStream(getLog(), true), true);
        com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler(out);
        CompilerOptions options = newCompilerOptions();

        if(!aggregation.isMinification()){ //Override the pretty print options if minification false
            getLog().info("Minification if false, Compilation Level is set to " + CompilationLevel.WHITESPACE_ONLY);
            CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
            options.setPrettyPrint(true);
            options.setPrintInputDelimiter(true);
            options.setInputDelimiter("// -- File: %name% ( Input %num% ) -- // ");
        } else {
            getLog().info("Compilation Level set to " + googleClosureCompilationLevel);
            googleClosureCompilationLevel.setOptionsForCompilationLevel(options);
            options.setPrettyPrint(googleClosurePrettyPrint);
            options.setPrintInputDelimiter(googleClosurePrettyPrint);
        }

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

        List<SourceFile> inputs = new ArrayList<>();
        for (String file : aggregation.getFiles()) {
            File theFile = new File(file);
            if (theFile.exists()) {
                inputs.add(SourceFile.fromFile(theFile));
            } else {
                File f = new File(getInternalAssetOutputDirectory(), file);
                if (!f.exists() && !f.getName().endsWith("js")) {
                    // Append the extension
                    f = new File(getInternalAssetOutputDirectory(), file + ".js");
                }

                if (!f.exists()) {
                    throw new WatchingException("Cannot compute aggregated JavaScript - the '"
                            + f.getAbsolutePath() + "' file does not exist");
                }

                inputs.add(SourceFile.fromFile(f));
            }
        }


        List<SourceFile> externs = new ArrayList<>();
        if (javascript.getExtern() != null) {
            externs.add(new SourceFile(javascript.getExtern().getAbsolutePath()));
        }

        compiler.initOptions(options);
        final Result result = compiler.compile(externs, inputs, options);
        listErrors(result);

        if (!result.success) {
            throw new WatchingException("Error while compile JavaScript files, check log for more details");
        }

        FileUtils.deleteQuietly(output);
        String[] outputs = compiler.toSourceArray();
        System.out.println("Writing " + outputs.length + " source");
        for (String source : outputs) {
            try {
                System.out.println(source);
                FileUtils.write(output, source, true);
            } catch (IOException e) {
                throw new WatchingException("Cannot write minified JavaScript file '" + output.getAbsolutePath() + "'",
                        e);
            }
        }
    }

    private File fixPath(File output) {
        if (output.isAbsolute()) {
            return output;
        } else {
            return new File(getInternalAssetOutputDirectory(), output.getPath());
        }
    }

    protected File getDefaultOutputFile(Aggregation aggregation) {
        String classifier = googleClosureMinifierSuffix;
        if (aggregation.isMinification()) {
            if (javascript.getMinifierSuffix() != null) {
                classifier = javascript.getMinifierSuffix();
            }
        } else {
            classifier = "";
        }
        return new File(getInternalAssetOutputDirectory(), project.getArtifactId() + classifier + ".js");
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "js", "coffee")
                        && isNotMinified(file)
                        && isNotInLibs(file);
    }

    public static boolean isNotInLibs(File file) {
        return !file.getAbsolutePath().contains("assets/libs/") &&
                // On windows:
                !file.getAbsolutePath().contains("assets\\libs\\");
    }

    public boolean isNotMinified(File file) {
        return !file.getName().endsWith("min.js")
                && !file.getName().endsWith(googleClosureMinifierSuffix + ".js");
    }

    public File getMinifiedFile(File file) {
        File output = getOutputFile(file);
        return new File(output.getParentFile().getAbsoluteFile(),
                output.getName().replace(".js", googleClosureMinifierSuffix + ".js"));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        if (javascript != null  && WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir))) {
            compile(javascript);
        } else if (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir))) {
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
        if (isNotMinified(file)) {
            File minified = getMinifiedFile(file);
            FileUtils.deleteQuietly(minified);
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

        Collection<File> files = FileUtils.listFiles(base, new String[]{"js"}, true);
        List<File> store = new ArrayList<>();
        List<SourceFile> inputs = new ArrayList<>();
        List<SourceFile> externs = new ArrayList<>();

        for (File file : files) {
            if (file.isFile() && isNotMinified(file) && isNotInLibs(file)) {
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
