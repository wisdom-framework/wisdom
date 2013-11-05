package org.ow2.chameleon.wisdom.test;

import org.apache.commons.io.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.ow2.chameleon.wisdom.test.internals.ChameleonExecutor;
import org.ow2.chameleon.wisdom.test.shared.InVivoRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The Wisdom Test Runner.
 */
public class WisdomRunner extends BlockJUnit4ClassRunner implements Filterable, Sortable {


    private final ChameleonExecutor executor;
    private final InVivoRunner delegate;
    private final File basedir;

    private static Logger LOGGER = LoggerFactory.getLogger(WisdomRunner.class);

    public WisdomRunner(Class<?> klass) throws Exception {
        super(klass);
        basedir = checkWisdomInstallation();
        File bundle = detectApplicationBundleIfExist();
        if (bundle != null  && bundle.exists()) {
            LOGGER.info("Application bundle found in the application directory (" + bundle.getAbsoluteFile() + "), " +
                    "deleting the file to allow test execution");
            bundle.delete();
        }

        System.setProperty("application.configuration",
                new File(basedir, "/conf/application.conf").getAbsolutePath());
        executor = ChameleonExecutor.instance(basedir);
        executor.deployProbe();

        delegate = executor.getInVivoRunnerInstance(klass);
    }

    /**
     * Detects if the application bundle is present in the application directory.
     * The detection stops when a jar file contains a class file from target/classes and where sizes are equals.
     * @return the application bundle if detected.
     * @throws IOException cannot open files.
     */
    private File detectApplicationBundleIfExist() throws IOException {
        File application = new File(basedir, "application");

        if (! application.isDirectory()) {
            return null;
        }

        File[] files = application.listFiles();
        if (files == null) {
            return null;
        }

        // Find one entry from classes.
        File classes = new File("target/classes");
        Collection<File> clazzes = FileUtils.listFiles(classes, new String[]{"class"}, true);

        // Iterate over the set of jar files.
        for (File file : files) {
            if (! file.getName().endsWith("jar")) {
                continue;
            }

            JarFile jar = new JarFile(file);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    for (File clazz : clazzes) {
                        if (entry.getName().endsWith(clazz.getName())
                                && entry.getSize() == clazz.length()) {
                            // Found !
                            return file;
                        }
                    }
                }
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

//        // Stop Wisdom whe everything is over.
//        try {
//            executor.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
