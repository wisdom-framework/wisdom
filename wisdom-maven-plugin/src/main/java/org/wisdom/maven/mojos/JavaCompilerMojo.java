package org.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.CompilerExecutor;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.Collection;

/**
 * A processor compiling java sources.
 */
@Mojo(name = "compile", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class JavaCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {
    public static final String JAVA_EXTENSION = ".java";
    private File classes;
    private CompilerExecutor compiler = new CompilerExecutor();

    @Override
    public void execute() throws MojoExecutionException {
        classes = new File(buildDirectory, "classes");
        compiler.execute(this);
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getJavaSource(basedir));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            execute();
        } catch (MojoExecutionException e) {
            throw new WatchingException("Compilation error", e);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        try {
            execute();
        } catch (MojoExecutionException e) {
            throw new WatchingException("Compilation error", e);
        }
        return true;
    }

    @Override
    public boolean fileDeleted(final File file) throws WatchingException {
        // Delete the associated class file.
        // We delete more than required... but the inner class case is very tricky.
        Collection<File> files = FileUtils.listFiles(classes, new IOFileFilter() {
            @Override
            public boolean accept(File test) {
                String classname = FilenameUtils.getBaseName(test.getName());
                String filename = FilenameUtils.getBaseName(file.getName());
                return classname.equals(filename) || classname.startsWith(filename + "$");
            }

            @Override
            public boolean accept(File dir, String name) {
                return accept(new File(dir, name));
            }
        }, TrueFileFilter.INSTANCE);

        for (File clazz : files) {
            clazz.delete();
        }

        try {
            execute();
        } catch (MojoExecutionException e) {
            throw new WatchingException("Compilation error", e);
        }
        return true;
    }
}
