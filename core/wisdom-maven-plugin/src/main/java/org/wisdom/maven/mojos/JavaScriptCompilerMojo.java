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
import org.apache.commons.compress.utils.IOUtils;
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

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;

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

    /**
     * Whether or not the output JavaScript should be pretty.
     */
    @Parameter(defaultValue = "false")
    public boolean googleClosurePrettyPrint;

    /**
     * Whether or not the Google Closure processing is skipped.
     */
    @Parameter(defaultValue = "${skipGoogleClosure}")
    public boolean skipGoogleClosure;

    /**
     * Minified file extension parameter, lets the user define their own extensions to use with
     * minification. Must not contain the {@literal .js} extension.
     */
    @Parameter(defaultValue = "-min")
    public String googleClosureMinifierSuffix;

    /**
     * Aggregated file delimiter. The string used as header of each aggregated files.
     * Works only with pretty print. {@literal %name%} and {@literal %num%} are substitute by the file full name and it's
     * input number respectively.
     */
    @Parameter(defaultValue = "// -- Input %num% -- //")
    public String googleClosureInputDelimiter;

    /**
     * Whether or not Google Closure must create the source map file.
     */
    @Parameter(defaultValue = "true")
    public boolean googleClosureMap;

    /**
     * The JavaScript configuration.
     */
    @Parameter
    protected JavaScript javascript;

    private File destinationForInternals;
    private File destinationForExternals;

    /**
     * The Error message prefix.
     */
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

        if (javaScript.getExtern() != null && !javaScript.getExtern().isFile()) {
            throw new WatchingException("The 'extern' file " + javaScript.getExtern().getAbsolutePath() + " does not " +
                    "exist");
        }

        for (Aggregation aggregation : javaScript.getAggregations()) {
            compile(aggregation);
        }

        // Cleanup if needed
        for (Aggregation aggregation : javaScript.getAggregations()) {
            if (aggregation.isRemoveIncludedFiles()) {
                for (File file : getFiles(aggregation)) {
                    File output = getOutputFile(aggregation);
                    // We must not remove output file.
                    if (! output.getAbsolutePath().equals(file.getAbsolutePath())) {
                        FileUtils.deleteQuietly(file);
                    }
                }
            }
        }
    }

    private void compile(Aggregation aggregation) throws WatchingException {
        File output = getOutputFile(aggregation);

        if (!output.getParentFile().isDirectory()) {
            getLog().debug("Create directory " + output.getParentFile().getAbsolutePath() + " : "
                    + output.getParentFile().mkdirs());
        }

        getLog().info("Compressing JavaScript files from aggregation "
                + aggregation.getSelectedFiles(getInternalAssetOutputDirectory())
                + " using Google Closure");
        PrintStream out = getPrintStreamToDumpLog();
        com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler(out);
        CompilerOptions options = newCompilerOptions();

        if (!aggregation.isMinification()) { //Override the pretty print options if minification false
            getLog().info("Minification if false, Compilation Level is set to " + CompilationLevel.WHITESPACE_ONLY);
            CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
            options.setPrettyPrint(true);
            options.setPrintInputDelimiter(true);
            options.setInputDelimiter(googleClosureInputDelimiter);
        } else {
            getLog().info("Compilation Level set to " + googleClosureCompilationLevel);
            googleClosureCompilationLevel.setOptionsForCompilationLevel(options);
            options.setPrettyPrint(googleClosurePrettyPrint);
            options.setPrintInputDelimiter(googleClosurePrettyPrint);
        }

        List<SourceFile> inputs = new ArrayList<>();
        final Collection<File> fileToAggregate = getFiles(aggregation);
        for (File file : fileToAggregate) {
            inputs.add(SourceFile.fromFile(file));
        }

        List<SourceFile> externs = new ArrayList<>();
        if (javascript.getExtern() != null) {
            externs.add(new SourceFile(javascript.getExtern().getAbsolutePath()));
        }

        if(googleClosureMap) {
            options.setSourceMapOutputPath(output.getParent());
            options.setSourceMapLocationMappings(
                     singletonList(new SourceMap.LocationMapping(output.getParent()+File.separator,"")));
        }

        compiler.initOptions(options);
        final Result result = compiler.compile(externs, inputs, options);
        listErrors(result);

        if (!result.success) {
            throw new WatchingException("Error while compile JavaScript files, check log for more details");
        }

        FileUtils.deleteQuietly(output);
        String[] outputs = compiler.toSourceArray();

        for (String source : outputs) {
            try {
                FileUtils.write(output, source, true);
            } catch (IOException e) {
                throw new WatchingException("Cannot write minified JavaScript file '" + output.getAbsolutePath() + "'",
                        e);
            }
        }

        //Create the source map file
        createSourceMapFile(output,compiler.getSourceMap());
    }

    private File getOutputFile(Aggregation aggregation) {
        File output;
        if (aggregation.getOutput() == null) {
            output = getDefaultOutputFile(aggregation);
        } else {
            output = new File(aggregation.getOutput());
            output = fixPath(output);
        }
        return output;
    }

    private Collection<File> getFiles(Aggregation aggregation) throws WatchingException {
        List<File> list = new ArrayList<>();

        if (aggregation.getFiles() != null  && ! aggregation.getFiles().isEmpty()) {
            for (String file : aggregation.getFiles()) {
                File theFile = new File(file);
                if (theFile.exists()) {
                    list.add(theFile);
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
                    list.add(f);
                }
            }
            return list;
        }

        // Else we use a file set.
        return aggregation.getSelectedFiles(getInternalAssetOutputDirectory());
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
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "js", "coffee")
                        && isNotMinified(file)
                        && isNotInLibs(file);
    }

    /**
     * Checks whether or not the given file is not in the 'libs' directory.
     *
     * @param file the file to check
     * @return {@code true} if the file is **not** in <em>assets/libs</em>, {@code false} otherwise.
     */
    public static boolean isNotInLibs(File file) {
        return !file.getAbsolutePath().contains("assets/libs/") &&
                // On windows:
                !file.getAbsolutePath().contains("assets\\libs\\");
    }

    /**
     * Checks whether or not the file is minified.
     *
     * @param file the file to check
     * @return {@code true} if the file is minified, {@code false} otherwise. This method only check for the file
     * extension.
     */
    public boolean isNotMinified(File file) {
        return !file.getName().endsWith("min.js")
                && !file.getName().endsWith(googleClosureMinifierSuffix + ".js");
    }

    /**
     * Computes the file object for the minified version of the given file. The given file must be a '.js' file.
     *
     * @param file the file
     * @return the associated minified file
     */
    public File getMinifiedFile(File file) {
        File output = getOutputFile(file);
        return new File(output.getParentFile().getAbsoluteFile(),
                output.getName().replace(".js", googleClosureMinifierSuffix + ".js"));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        if (javascript != null && WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir))) {
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
        PrintStream out = getPrintStreamToDumpLog();
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
        File minified;
        for (int i = 0; i < store.size(); i++) {
            try {
                minified = getMinifiedFile(store.get(i));
                FileUtils.write(minified, outputs[i]);
            } catch (IOException e) {
                throw new WatchingException("Cannot write minified JavaScript file : " + getMinifiedFile(store.get(i)), e);
            }
        }

    }

    private PrintStream getPrintStreamToDumpLog() {
        try {
            return new PrintStream(new LoggedOutputStream(getLog(), true), true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This should not happen as the UTF-8 encoding is mandatory for the JVM.
            throw new IllegalArgumentException("UTF-8 not supported");
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

        //Initialise source map options
        if (googleClosureMap) {
            options.setSourceMapFormat(SourceMap.Format.DEFAULT);
        }

        return options;
    }

    /**
     * Create a source map file corresponding to the given compiled js file.
     *
     * @param output The compiled js file
     * @param sourceMap The {@link SourceMap} retrieved from the compiler
     * @throws WatchingException If an IOException occurred while creating the source map file.
     */
    private void createSourceMapFile(File output,SourceMap sourceMap) throws WatchingException{
        if (googleClosureMap) {
            PrintWriter mapWriter = null;
            File mapFile = new File(output.getPath() + ".map");
            FileUtils.deleteQuietly(mapFile);

            try {
                mapWriter = new PrintWriter(mapFile, Charset.defaultCharset().name());
                sourceMap.appendTo(mapWriter, output.getName());
                FileUtils.write(output, "\n//# sourceMappingURL=" + mapFile.getName(), true);
            } catch (IOException e) {
                throw new WatchingException("Cannot create source map file for JavaScript file '" +
                        output.getAbsolutePath() + "'", e);
            } finally {
                IOUtils.closeQuietly(mapWriter);
            }
        }
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
