package org.ow2.chameleon.wisdom.maven.processors;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.utils.DefensiveThreadFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline
 */
public class Pipeline {

    private List<Processor> processors = new ArrayList<>();
    private AbstractWisdomMojo mojo;
    private FileAlterationMonitor watcher;

    public Pipeline addLast(Processor processor) {
        processors.add(processor);
        return this;
    }

    public Pipeline remote(Processor processor) {
        processors.remove(processor);
        return this;
    }

    public Pipeline initialize(AbstractWisdomMojo mojo) throws ProcessorException {
        this.mojo = mojo;
        for (Processor processor : processors) {
            processor.configure(mojo);
        }
        for (Processor processor : processors) {
            try {
                processor.processAll();
            } catch (ProcessorException e) {
                mojo.getLog().error("Error when initializing the pipeline - initialization aborted");
                throw e;
            }
        }
        return this;
    }

    public Pipeline watch() {
        watcher = new FileAlterationMonitor(2000);
        watcher.setThreadFactory(new DefensiveThreadFactory("wisdom-pipeline-watcher", mojo));
        FileAlterationObserver observer = new FileAlterationObserver(new File("src/main"), TrueFileFilter.INSTANCE);
        observer.addListener(new PipelineWatcher(this));
        watcher.addObserver(observer);
        try {
            watcher.start();
        } catch (Exception e) {
            mojo.getLog().error("Cannot start the watcher", e);
        }
        return this;
    }

    public Pipeline tearDown() {
        if (watcher != null) {
            try {
                watcher.stop();
            } catch (Exception e) {
                mojo.getLog().error("Error while stopping the pipeline watcher", e);
            }
            watcher = null;
        }
        for (Processor processor : processors) {
            processor.tearDown();
        }
        mojo = null;
        return this;
    }

    public void onFileCreate(File file) {
        for (Processor processor : processors) {
            try {
                if (processor.accept(file)) {
                    if (!processor.fileCreated(file)) {
                        return;
                    }
                }
            } catch (ProcessorException e) {
                mojo.getLog().error(e.getMessage(), e);
            }
        }
    }

    public void onFileChange(File file) {
        for (Processor processor : processors) {
            try {
                if (processor.accept(file)) {
                    if (!processor.fileUpdated(file)) {
                        return;
                    }
                }
            } catch (ProcessorException e) {
                mojo.getLog().error(e.getMessage(), e);
            }
        }
    }

    public void onFileDelete(File file) {
        for (Processor processor : processors) {
            try {
                if (processor.accept(file)) {
                    if (!processor.fileDeleted(file)) {
                        return;
                    }
                }
            } catch (ProcessorException e) {
                mojo.getLog().error(e.getMessage(), e);
            }
        }
    }
}
