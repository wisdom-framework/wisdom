package org.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.Collection;

/**
 * Compiles less files.
 */
@Mojo(name = "compile-less", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class LessCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {

    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;

    @Override
    public void execute() throws MojoExecutionException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        NPM.Install install = new NPM.Install(this);
        install.install("less", "1.5.0"); //TODO constants.

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
            new NPM.Execution(this).npm("less").command("lessc").args(file.getAbsolutePath(),
                    out.getAbsolutePath()).withoutQuoting().execute();
        } catch (MojoExecutionException e) { //NOSONAR
            throw new WatchingException("Error during the compilation of " + file.getName() + " : " + e.getMessage());
        }

        if (!out.isFile()) {
            throw new WatchingException("Error during the compilation of " + file.getAbsoluteFile() + " check log");
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
        if (theFile.exists()) {
            theFile.delete();
        }
        return true;
    }

}
