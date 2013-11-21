package org.ow2.chameleon.wisdom.asciidoc;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.asciidoctor.*;
import org.asciidoctor.extension.ExtensionRegistry;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.internal.DocumentRuby;
import org.asciidoctor.internal.PreprocessorReader;
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.WatchingException;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomWatcherMojo;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;


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
    protected String doctype = "article";
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
    @Parameter(property = "sourceDocumentName", required = false)
    protected File sourceDocumentName;
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


    File processedFile;
    DocumentHeader header;

    public void execute()
            throws MojoExecutionException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        if (instance == null) {
            instance = getAsciidoctorInstance();
        }

        final OptionsBuilder optionsBuilder = OptionsBuilder.options().toDir(destinationForExternals).compact(compact)
                .safe(SafeMode.UNSAFE).eruby(eruby).backend(backend).docType(doctype).headerFooter(headerFooter);

        if (templateEngine != null) {
            optionsBuilder.templateEngine(templateEngine);
        }

        if (templateDir != null) {
            optionsBuilder.templateDir(templateDir);
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

        optionsBuilder.attributes(attributes);



        if (sourceDocumentName == null) {
            for (final File f : scanSourceFiles(externalSources)) { // TODO Do the same with internals.
                renderFile(instance, optionsBuilder.asMap(), f);
            }
        } else {
            renderFile(instance, optionsBuilder.asMap(), sourceDocumentName);
        }
    }

    protected Asciidoctor getAsciidoctorInstance() throws MojoExecutionException {
        return Asciidoctor.Factory.create();
    }

    private List<File> scanSourceFiles(File root) {
        final List<File> asciidoctorFiles;
        if (extensions == null || extensions.isEmpty()) {
            final DirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(root.getAbsolutePath());
            asciidoctorFiles = directoryWalker.scan();
        } else {
            final DirectoryWalker directoryWalker = new CustomExtensionDirectoryWalker(root.getAbsolutePath(), extensions);
            asciidoctorFiles = directoryWalker.scan();
        }
        return asciidoctorFiles;
    }

    protected void renderFile(Asciidoctor asciidoctorInstance, Map<String, Object> options, File f) {
        getLog().info("Compiling Ascidoc file >> " + f.getName());
        this.processedFile = f;
        this.header = asciidoctorInstance.readDocumentHeader(f);

        asciidoctorInstance.renderFile(f, options);

        this.processedFile = null;
        this.header = null;
    }

    @Override
    public boolean accept(File file) {
        if (extensions == null || extensions.isEmpty()) {
            return file.getName().endsWith(".ad")  || file.getName().endsWith(".adoc")  || file.getName().endsWith
                    (".asciidoc");
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
