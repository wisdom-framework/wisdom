package org.ow2.chameleon.wisdom.test;

import org.junit.runner.Description;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.ow2.chameleon.wisdom.test.impl.ChameleonExecutor;

import java.io.File;

/**
 * The Wisdom Test Runner.
 */
public class WisdomRunner extends BlockJUnit4ClassRunner implements Filterable, Sortable {


    private final ChameleonExecutor executor;
    private final InVivoRunner delegate;
    private final File basedir;

    public WisdomRunner(Class<?> klass) throws Exception {
        super(klass);
        basedir = checkWisdomInstallation();
        executor = new ChameleonExecutor();
        System.setProperty("application.configuration",
                new File(basedir,"/conf/application.conf").getAbsolutePath());
        executor.start(basedir);

        executor.deployProbe();

        delegate = executor.getInVivoRunnerInstance(klass);
    }

    private File checkWisdomInstallation() {
        File directory = new File("target/wisdom");
        if (! directory.isDirectory()) {
            throw new ExceptionInInitializerError("Wisdom is not installed in " + directory.getAbsolutePath() + " - " +
                    "please check your execution directory, and that Wisdom is prepared correctly. To setup Wisdom, " +
                    "run 'mvn pre-integration-test' from your application directory");
        }
        File conf = new File(directory, "conf/application.conf");
        if (! conf.isFile()) {
            throw new ExceptionInInitializerError("Wisdom is not correctly installed in " + directory.getAbsolutePath()
                    + " - the configuration file does not exist. Please check your Wisdom runtime. To setup Wisdom, " +
                    "run 'mvn clean pre-integration-test' from your application directory");
        }
        return directory;
    }

    @Override
    protected Object createTest() throws Exception {
        Object o = delegate.createTest();
        System.out.println("Creating test " + o);
        return o;
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
        // Stop Wisdom whe everything is over.
        try {
            executor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
