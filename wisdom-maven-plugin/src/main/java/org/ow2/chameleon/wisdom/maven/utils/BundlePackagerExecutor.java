package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;
import org.ow2.chameleon.testing.tinybundles.ipojo.IPOJOStrategy;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.processors.ProcessorException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ops4j.pax.tinybundles.core.TinyBundles.*;


/**
 * Executes the maven-compiler-plugin.
 */
public class BundlePackagerExecutor {


    public void execute(AbstractWisdomMojo mojo, File out) {
        TinyBundle tested = TinyBundles.bundle();


        // We look inside target/classes to find the class and resources
        File classes = new File("target/classes");
        Collection<File> files = FileUtils.listFilesAndDirs(classes, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        // Automatic detection of 'service(s) packages'
        List<String> exports = new ArrayList<String>();
        for (File file : files) {
            if (file.isDirectory()) {
                // By convention we export of .services and .service package
                if (file.getAbsolutePath().contains("/services")  || file.getAbsolutePath().contains("/service")) {
                    String path = file.getAbsolutePath().substring(classes.getAbsolutePath().length() +1);
                    String packageName = path.replace('/', '.');
                    exports.add(packageName);
                }
            } else {
                // We need to compute the path
                String path = file.getAbsolutePath().substring(classes.getAbsolutePath().length() +1);
                try {
                    tested.add(path, file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // Ignore it.
                }
            }
        }

        String clause = "";
        for (String export : exports) {
            if (export.length() > 0) { export += ", "; }
            clause += export;
        }
        if (! clause.isEmpty()) {
            tested.set(Constants.EXPORT_PACKAGE, clause);
        }

        InputStream inputStream = tested
                .set(Constants.BUNDLE_SYMBOLICNAME, mojo.project.getArtifactId())
                .set(Constants.IMPORT_PACKAGE, "*")
                .build(IPOJOStrategy.withiPOJO(new File("src/main/resources")));

        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, out);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot compute the url of the manipulated bundle");
        } catch (IOException e) {
            throw new RuntimeException("Cannot write of the manipulated bundle");
        }
    }
}
