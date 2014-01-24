package org.wisdom.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.runner.Description;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.test.internals.ChameleonExecutor;
import org.wisdom.test.shared.InVivoRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * The Wisdom Test Runner.
 */
public class WisdomRunner extends BlockJUnit4ClassRunner implements Filterable, Sortable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomRunner.class);
    private final InVivoRunner delegate;

    public WisdomRunner(Class<?> klass) throws Exception {
        super(klass);
        File basedir = checkWisdomInstallation();
        File bundle = detectApplicationBundleIfExist(new File(basedir, "application"));
        if (bundle != null && bundle.exists()) {
            LOGGER.info("Application bundle found in the application directory (" + bundle.getAbsoluteFile() + "), " +
                    "the bundle will be deleted and replaced by the tested bundle (with the very same content).");
            bundle.delete();
        }
        bundle = detectApplicationBundleIfExist(new File(basedir, "runtime"));
        if (bundle != null && bundle.exists()) {
            LOGGER.info("Application bundle found in the runtime directory (" + bundle.getAbsoluteFile() + "), " +
                    "the bundle will be deleted and replaced by the tested bundle (with the very same content).");
            bundle.delete();
        }

        System.setProperty("application.configuration",
                new File(basedir, "/conf/application.conf").getAbsolutePath());
        ChameleonExecutor executor = ChameleonExecutor.instance(basedir);

        executor.deployApplication();
        executor.deployProbe();

        delegate = executor.getInVivoRunnerInstance(klass);
    }

    /**
     * Checks if a file having somewhat the current tested application name is contained in the given directory. This
     * method follows the default maven semantic. The final file is expected to have a name compliant with the
     * following rules: <code>artifactId-version.jar</code>. If the version ends with <code>-SNAPSHOT</code>,
     * it just checks for <code>artifactId-stripped_version</code>, where stripped version is the version without the
     * <code>SNAPSHOT</code> part.
     * <p/>
     * The artifactId and version are read from the <code>target/osgi/osgi.properties</code> file,
     * that should have been written by the Wisdom build process.
     *
     * @param directory the directory
     * @return the bundle file if found
     * @throws IOException if something bad happens.
     */
    private File detectApplicationBundleIfExist(File directory) throws IOException {
        Properties properties = getMavenProperties();
        if (properties == null || directory == null || !directory.isDirectory()) {
            return null;
        }

        final String artifactId = properties.getProperty("project.artifactId");
        String version = properties.getProperty("project.version");
        final String strippedVersion;
        if (version.endsWith("-SNAPSHOT")) {
            strippedVersion = version.substring(0, version.length() - "-SNAPSHOT".length());
        } else {
            strippedVersion = version;
        }

        Iterator<File> files = FileUtils.iterateFiles(directory, new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile()
                        && file.getName().startsWith(artifactId + "-" + strippedVersion)
                        && file.getName().endsWith(".jar");
            }
        }, TrueFileFilter.INSTANCE);

        if (files.hasNext()) {
            return files.next();
        }
        return null;
    }

    /**
     * We should have generated a target/osgi/osgi.properties file will all the metadata we inherit from Maven.
     *
     * @return the properties read from the file.
     */
    private static Properties getMavenProperties() throws IOException {
        File osgi = new File("target/osgi/osgi.properties");
        if (osgi.isFile()) {
            FileInputStream fis = null;
            try {
                Properties read = new Properties();
                fis = new FileInputStream(osgi);
                read.load(fis);
                return read;
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
        return null;
    }

    private File checkWisdomInstallation() {
        File directory = new File("target/wisdom");
        if (!directory.isDirectory()) {
            throw new ExceptionInInitializerError("Wisdom is not installed in " + directory.getAbsolutePath() + " - " +
                    "please check your execution directory, and that Wisdom is prepared correctly. To setup Wisdom, " +
                    "run 'mvn pre-integration-test' from your application directory");
        }
        File conf = new File(directory, "conf/application.conf");
        if (!conf.isFile()) {
            throw new ExceptionInInitializerError("Wisdom is not correctly installed in " + directory.getAbsolutePath()
                    + " - the configuration file does not exist. Please check your Wisdom runtime. To setup Wisdom, " +
                    "run 'mvn clean pre-integration-test' from your application directory");
        }
        return directory;
    }

    @Override
    protected Object createTest() throws Exception {
        return delegate.createTest();
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        delegate.filter(filter);
    }

    @Override
    public void sort(Sorter sorter) {
        delegate.sort(sorter);
    }
}
