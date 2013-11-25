package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.WatchingException;
import org.ow2.chameleon.wisdom.maven.node.NPM;
import org.ow2.chameleon.wisdom.maven.utils.WatcherUtils;

import java.io.File;

/**
 * Compiles coffeescript files.
 */
@Mojo(name = "compile-coffeescript", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class CoffeeScriptCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {

    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        NPM.Install install = new NPM.Install(this);
        install.install("coffee-script", "1.6.3"); //TODO constants.

        if (internalSources.isDirectory()) {
            getLog().info("Compiling coffeescript files from " + internalSources.getAbsolutePath());
            new NPM.Execution(this).npm("coffee-script").command("coffee").withoutQuoting()
                    .args("--compile", "--map", "--output",
                    destinationForInternals.getAbsolutePath(), internalSources.getAbsolutePath()).execute();
        }

        if (externalSources.isDirectory()) {
            getLog().info("Compiling coffeescript files from " + externalSources.getAbsolutePath());
            new NPM.Execution(this).npm("coffee-script").command("coffee").withoutQuoting()
                    .args("--compile", "--map", "--output",
                            destinationForExternals.getAbsolutePath(), externalSources.getAbsolutePath()).execute();
        }
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir)))
                )
                && WatcherUtils
                        .hasExtension(file, "coffee");
    }

    private File getOutputJSFile(File input) {
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

        String jsFileName = input.getName().substring(0, input.getName().length() - ".coffee".length()) + ".js";
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + jsFileName);
    }

    private void compile(File file) throws WatchingException {
        File out = getOutputJSFile(file);
        if (file == null) {
            return;
        }
        getLog().info("Compiling " + file.getAbsolutePath() + " to " + out.getAbsolutePath());

        try {
            new NPM.Execution(this).npm("coffee-script").command("coffee").withoutQuoting()
                    .args("--compile", "--map", "--output", out.getParentFile().getAbsolutePath(),
                            file.getAbsolutePath()).execute();
        } catch (MojoExecutionException e) {
            throw new WatchingException("Error during the compilation of " + file.getName() + " : " + e.getMessage());
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
        File theFile = getOutputJSFile(file);
        if (theFile.exists()) {
            theFile.delete();
        }
        return true;
    }

}
