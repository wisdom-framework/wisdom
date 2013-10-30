package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;

/**
 * Copy all compile dependencies from the project (excluding transitive) to the application directory.
 * Only bundles are copied.
 * If the bundle is already present in 'runtime', the file is not copied.
 */
public class DependencyCopy {

    public static void copy(AbstractWisdomMojo mojo) throws IOException {
        File applicationDirectory = new File(mojo.getWisdomRootDirectory(), "application");
        File runtimeDirectory = new File(mojo.getWisdomRootDirectory(), "runtime");
        File coreDirectory = new File(mojo.getWisdomRootDirectory(), "core");

        // No transitive.
        Set<Artifact> artifacts = mojo.project.getDependencyArtifacts();
        for (Artifact artifact : artifacts) {
            if ("compile".equalsIgnoreCase(artifact.getScope())) {
                File file = artifact.getFile();

                // Check it's a 'jar file'
                if (! file.getName().endsWith(".jar")) {
                    mojo.getLog().info("Dependency " + file.getName() + " not copied - it does not look like a jar " +
                            "file");
                    continue;
                }

                // Do we already have this file in core or runtime ?
                File test = new File(coreDirectory, file.getName());
                if (test.exists()) {
                    mojo.getLog().info("Dependency " + file.getName() + " not copied - already existing in `core`");
                    continue;
                }
                test = new File(runtimeDirectory, file.getName());
                if (test.exists()) {
                    mojo.getLog().info("Dependency " + file.getName() + " not copied - already existing in `runtime`");
                    continue;
                }
                // Check that it's a bundle.
                if (! isBundle(file)) {
                    mojo.getLog().info("Dependency " + file.getName() + " not copied - it's not a bundle");
                    continue;
                }

                // All check done, let's copy !
                FileUtils.copyFileToDirectory(file, applicationDirectory);
            }
        }
    }
}
