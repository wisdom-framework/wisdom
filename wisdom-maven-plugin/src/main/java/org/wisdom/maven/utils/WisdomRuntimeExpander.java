package org.wisdom.maven.utils;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.wisdom.maven.Constants;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;

/**
 * Downloads and Expands the Wisdom Runtime.
 */
public class WisdomRuntimeExpander {


    public static boolean expand(AbstractWisdomMojo mojo, File destination) throws MojoExecutionException {
        if (destination.exists() && isWisdomAlreadyInstalled(destination)) {
            return false;
        }
        File archive;
        if (mojo.useBaseRuntime) {
            archive = DependencyFinder.resolve(mojo, mojo.plugin.getGroupId(),
                    Constants.WISDOM_BASE_RUNTIME_ARTIFACT_ID, mojo.plugin.getVersion(),
                    "zip");
        } else {
            archive = DependencyFinder.resolve(mojo, mojo.plugin.getGroupId(),
                    Constants.WISDOM_RUNTIME_ARTIFACT_ID, mojo.plugin.getVersion(),
                    "zip");
        }
        if (archive == null || !archive.exists()) {
            throw new MojoExecutionException("Cannot retrieve the Wisdom-Runtime file");
        }
        unzip(mojo, archive, destination);
        ensure(destination);
        return true;
    }

    private static boolean isWisdomAlreadyInstalled(File destination) {
        File bin = new File(destination, "bin");
        File core = new File(destination, "core");
        File runtime = new File(destination, "runtime");

        return bin.isDirectory() && core.isDirectory() && runtime.isDirectory();
    }

    private static void ensure(File destination) throws MojoExecutionException {
        if (!isWisdomAlreadyInstalled(destination)) {
            throw new MojoExecutionException("The installation of Wisdom in " + destination.getAbsolutePath() + " has" +
                    " failed");
        }
    }

    private static void unzip(final AbstractWisdomMojo mojo, File in, File out) {
        ZipUnArchiver unarchiver = new ZipUnArchiver(in);
        unarchiver.enableLogging(new PlexusLoggerWrapper(mojo.getLog()));
        unarchiver.setDestDirectory(out);
        unarchiver.extract();
    }


}
