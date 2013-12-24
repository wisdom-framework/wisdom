package org.wisdom.maven.pipeline;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.plugin.Mojo;
import org.wisdom.maven.Watcher;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.DefensiveThreadFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline.
 */
public class Pipeline {

    private List<Watcher> watchers = new ArrayList<>();
    private final Mojo mojo;
    private FileAlterationMonitor watcher;
    private final File baseDir;

    private final static String WATCHING_EXCEPTION_MESSAGE = "Watching exception: %s (check log for more details)";

    public Pipeline(Mojo mojo, File baseDir) {
        this.mojo = mojo;
        this.baseDir = baseDir;
    }

    public Pipeline(Mojo mojo, File baseDir, List<? extends Watcher> list) {
        this(mojo, baseDir);
        mojo.getLog().debug("Initializing watch mode with " + list);
        watchers = new ArrayList<>();
        for (Object o : list) {
            watchers.add(new WatcherDelegate(o));
        }
    }

    public void shutdown() {
        try {
            watcher.stop();
        } catch (Exception e) { //NOSONAR
            // ignore it.
        }
    }

    public Pipeline remove(Watcher watcher) {
        watchers.remove(watcher);
        return this;
    }

    public Pipeline watch() {
        watcher = new FileAlterationMonitor(2000);
        watcher.setThreadFactory(new DefensiveThreadFactory("wisdom-pipeline-watcher", mojo));
        FileAlterationObserver srcObserver = new FileAlterationObserver(new File(baseDir, "src/main"), TrueFileFilter.INSTANCE);
        PipelineWatcher listener = new PipelineWatcher(this);
        srcObserver.addListener(listener);
        watcher.addObserver(srcObserver);
        try {
            mojo.getLog().info("Start watching " + baseDir.getAbsolutePath());
            watcher.start();
        } catch (Exception e) {
            mojo.getLog().error("Cannot start the watcher", e);
        }
        return this;
    }

    public void onFileCreate(File file) {
        mojo.getLog().info("");
        mojo.getLog().info("The watcher has detected a new file: " + file.getAbsolutePath());
        mojo.getLog().info("");
        for (Watcher watcher : watchers) {
            if (watcher.accept(file)) {
                // This flag will be set to false if the processing must be interrupted.
                boolean continueProcessing;
                try {
                    continueProcessing = watcher.fileCreated(file);
                } catch (WatchingException e) { //NOSONAR
                    mojo.getLog().error(String.format(WATCHING_EXCEPTION_MESSAGE, e.getMessage()));
                    continueProcessing = false;
                }
                if (!continueProcessing) {
                    break;
                }
            }
        }
        mojo.getLog().info("");
        mojo.getLog().info("");
    }

    public void onFileChange(File file) {
        mojo.getLog().info("");
        mojo.getLog().info("The watcher has detected a change in " + file.getAbsolutePath());
        mojo.getLog().info("");
        for (Watcher watcher : watchers) {
            if (watcher.accept(file)) {
                // This flag will be set to false if the processing must be interrupted.
                boolean continueProcessing;
                try {
                    continueProcessing = watcher.fileUpdated(file);
                } catch (WatchingException e) { //NOSONAR
                    mojo.getLog().error(String.format(WATCHING_EXCEPTION_MESSAGE, e.getMessage()));
                    continueProcessing = false;
                }
                if (!continueProcessing) {
                    break;
                }
            }
        }
        mojo.getLog().info("");
        mojo.getLog().info("");
    }

    public void onFileDelete(File file) {
        mojo.getLog().info("");
        mojo.getLog().info("The watcher has detected a deleted file: " + file.getAbsolutePath());
        mojo.getLog().info("");
        for (Watcher watcher : watchers) {
            if (watcher.accept(file)) {
                // This flag will be set to false if the processing must be interrupted.
                boolean continueProcessing;
                try {
                    continueProcessing = watcher.fileDeleted(file);
                } catch (WatchingException e) { //NOSONAR
                    mojo.getLog().error(String.format(WATCHING_EXCEPTION_MESSAGE, e.getMessage()));
                    continueProcessing = false;
                }
                if (!continueProcessing) {
                    break;
                }
            }
        }
        mojo.getLog().info("");
        mojo.getLog().info("");
    }
}
