package org.wisdom.ebean.mojo;

import com.avaje.ebean.enhance.agent.Transformer;
import com.avaje.ebean.enhance.ant.OfflineFileTransform;
import com.avaje.ebean.enhance.ant.TransformationListener;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.wisdom.maven.utils.BundlePackager;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Mojo executing the Ebean class enhancer and instructing the bundle packager to add a new Manifest header listing
 * the entities.
 * <p/>
 * This mojo is an adaption from the original Ebean mojo (avaje-ebeanorm-mavenenhancer)
 */
@Mojo(name = "ebean-enhance", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class EbeanTransformerMojo extends AbstractWisdomWatcherMojo {


    /**
     * Set the directory holding the class files we want to transform.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private String classSource;

    /**
     * Set the destination directory where we will put the transformed classes.
     * <p>
     * This is commonly the same as the classSource directory.
     * </p>
     */
    @Parameter
    String classDestination;

    /**
     * Set the arguments passed to the transformer.
     */
    @Parameter
    String transformArgs;

    /**
     * Set the package name to search for classes to transform.
     * <p>
     * If the package name ends in "/**" then this recursively transforms all sub
     * packages as well.
     * </p>
     */
    @Parameter
    String packages;

    public void execute() throws MojoExecutionException {

        final Log log = getLog();
        if (classSource == null) {
            classSource = "target/classes";
        }

        if (classDestination == null) {
            classDestination = classSource;
        }

        File f = new File("");
        log.info("Current Directory: " + f.getAbsolutePath());

        Transformer t = new Transformer(classSource, transformArgs);
        ClassLoader cl = this.getClass().getClassLoader();

        log.info("classSource=" + classSource + "  transformArgs=" + transformArgs
                + "  classDestination=" + classDestination + "  packages=" + packages);

        OfflineFileTransform ft = new OfflineFileTransform(t, cl, classSource, classDestination);
        final List<String> classes = new ArrayList<>();
        ft.setListener(new TransformationListener() {

            public void logEvent(String msg) {
                // We detect enhanced file by parsing the message.
                if (msg.startsWith("Enhanced ")) {
                    String path = extractPathFromAgentMessage(msg);
                    String classname = getClassName(path, classSource);
                    classes.add(classname);
                }
                log.info(msg);
            }

            public void logError(String msg) {
                log.error(msg);
            }
        });
        ft.process(packages);

        if (! classes.isEmpty()) {
            String header = computeHeader(classes);
            try {
                BundlePackager.addExtraHeaderToBundleManifest(basedir, "Ebean-Entities", header);
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot write the extra header 'Ebean-Entities", e);
            }
        }

        for (String names : classes) {

        }
    }

    public static String computeHeader(List<String> classes) {
        StringBuilder sb = new StringBuilder();
        for (String s : classes) {
            if (sb.length() == 0) {
                sb.append(s);
            } else {
                sb.append(", ").append(s);
            }
        }
        return sb.toString();
    }

    public static String extractPathFromAgentMessage(String msg) {
        return msg.substring("Enhanced ".length());
    }

    public static String getClassName(String path, String classSource) {
        path = path.substring(classSource.length() + 1);
        path = path.substring(0, path.length() - ".class".length());
        // for windows... replace the
        return path.replace("\\", ".").replace("/", ".");
    }


    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".java")
                && WatcherUtils.isInDirectory(file, WatcherUtils.getJavaSource(basedir));
    }

    // We can delegate all watcher callbacks to 'execute' as we use the same trigger as the java compiler.
    // That means that the Java compiler would have already removed or updated the .class files, making
    // our job much easier.

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            execute();
        } catch (MojoExecutionException e) {
            throw new WatchingException("An exception occurred while enhancing Java classes for Ebean", file, e);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        return fileCreated(file);
    }
}
