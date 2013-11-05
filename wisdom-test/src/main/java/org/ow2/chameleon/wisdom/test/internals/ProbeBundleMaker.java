package org.ow2.chameleon.wisdom.test.internals;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;
import org.ow2.chameleon.testing.tinybundles.ipojo.IPOJOStrategy;
import org.ow2.chameleon.wisdom.test.parents.*;
import org.ow2.chameleon.wisdom.test.probe.Activator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class responsible for creating the probe bundle.
 * The probe bundle contains both application files and the test files.
 * Such choice is made to avoid classloading issues when accessing controllers.
 */
public class ProbeBundleMaker {


    public static final String BUNDLE_NAME = "wisdom-probe-bundle";

    public static InputStream probe() throws IOException {
        TinyBundle bundle = TinyBundles.bundle();
        addProbeFiles(bundle);

        // We look inside target/classes to find the class and resources
        File tests = new File("target/test-classes");
        File classes = new File("target/classes");
        List<String> exports = new ArrayList();
        exports.addAll(insert(bundle, tests));
        exports.addAll(insert(bundle, classes));

        String clause = "";
        for (String export : exports) {
            if (export.length() > 0) { export += ", "; }
            clause += export;
        }

        if (clause.length() > 0) {
            bundle.set(Constants.EXPORT_PACKAGE, clause);
        }

        return bundle
                .set(Constants.BUNDLE_SYMBOLICNAME, BUNDLE_NAME)
                .set(Constants.IMPORT_PACKAGE, "*")
                .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                .set(Constants.BUNDLE_ACTIVATOR, Activator.class.getName())
                .build(IPOJOStrategy.withiPOJO(new File("src/main/resources")));
    }

    private static List<String> insert(TinyBundle bundle, File directory) {
        Collection<File> files = FileUtils.listFilesAndDirs(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        List<String> exports = new ArrayList<String>();
        for (File file : files) {
            if (file.isDirectory()) {
                // By convention we export of .services and .service package
                if (file.getAbsolutePath().contains("/services")  || file.getAbsolutePath().contains("/service")) {
                    String path = file.getAbsolutePath().substring(directory.getAbsolutePath().length() +1);
                    String packageName = path.replace('/', '.');
                    exports.add(packageName);
                }
            } else if (isNotAUnitTest(file)) {
                // We need to compute the path
                String path = file.getAbsolutePath().substring(directory.getAbsolutePath().length() +1);
                try {
                    bundle.add(path, file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // Ignore it.
                }
            }
        }
        return exports;
    }

    private static void addProbeFiles(TinyBundle tested) {
        tested.add(Activator.class);

        tested.add(Action.class);
        tested.add(Action.ActionResult.class);
        tested.add(ControllerTest.class);
        tested.add(DependencyInjector.class);
        tested.add(FakeContext.class);
        tested.add(FakeFileItem.class);
        tested.add(FakeFlashCookie.class);
        tested.add(FakeSessionCookie.class);
        tested.add(Filter.class);
        tested.add(Invocation.class);
        tested.add(Name.class);
    }

    /**
     * Checks that the given file is not a unit test according to the Maven convention.
     * @param file the file
     * @return true if the file is not starting or ending with 'test' or ending with 'testcase'
     */
    private static boolean isNotAUnitTest(File file) {
        String name = file.getName().toLowerCase();
        return ! name.startsWith("test")
                && ! name.endsWith("test.class")
                && ! name.endsWith("testcase.class")
                // Inner classes
                && ! name.contains("test$")
                && ! name.contains("testcase$");
    }

}
