package org.ow2.chameleon.wisdom.test.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;
import org.ow2.chameleon.testing.tinybundles.ipojo.IPOJOStrategy;
import org.ow2.chameleon.wisdom.test.probe.Activator;
import org.ow2.chameleon.wisdom.test.InVivoRunner;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class responsible for creating the probe bundle.
 */
public class ProbeBundleMaker {


    private static final String BUNDLE_NAME = "wisdom-probe-bundle";

    public static InputStream probe() {
        TinyBundle tested = TinyBundles.bundle();

        // Add the probe classes.
        tested.add(Activator.class);

        // We look inside target/classes to find the class and resources
        File classes = new File("target/test-classes");
        Collection<File> files = FileUtils.listFilesAndDirs(classes, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
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
                System.out.println(file.getName() + " added to " + path);
            }
        }

        String clause = "";
        for (String export : exports) {
            if (export.length() > 0) { export += ", "; }
            clause += export;
        }

        if (clause.length() > 0) {
            tested.set(Constants.EXPORT_PACKAGE, clause);
        }

        InputStream inputStream = tested
                .set(Constants.BUNDLE_SYMBOLICNAME, BUNDLE_NAME)
                .set(Constants.IMPORT_PACKAGE, "*")
                .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                .set(Constants.BUNDLE_ACTIVATOR, Activator.class.getName())
                .build(IPOJOStrategy.withiPOJO(new File("src/test/resources")));

        return inputStream;
    }

}
