package org.ow2.chameleon.wisdom.maven.pipeline;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.plugin.Mojo;
import org.ow2.chameleon.wisdom.maven.Watcher;
import org.ow2.chameleon.wisdom.maven.WatchingException;
import org.ow2.chameleon.wisdom.maven.utils.DefensiveThreadFactory;

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

    public Pipeline(Mojo mojo) {
        this.mojo = mojo;
    }

    public Pipeline(Mojo mojo, List<Watcher> list) {
        this(mojo);
        mojo.getLog().debug("Initializing watch mode with " + list);
        watchers = new ArrayList<>();
        for (Object o : list) {
            watchers.add(new WatcherDelegate(o));
        }
    }

    public void shutdown() {
        try {
            watcher.stop();
        } catch (Exception e) {
            // ignore it.
        }
    }

    public Pipeline addAll(List<Watcher> watchers) {
        this.watchers = new ArrayList<>();
        for (Object o : watchers) {
            this.watchers.add(new WatcherDelegate(o));
        }
        return this;
    }

    public Pipeline addLast(Watcher watcher) {
        watchers.add(watcher);
        return this;
    }

    public Pipeline remove(Watcher watcher) {
        watchers.remove(watcher);
        return this;
    }

    public Pipeline watch() {
        watcher = new FileAlterationMonitor(2000);
        watcher.setThreadFactory(new DefensiveThreadFactory("wisdom-pipeline-watcher", mojo));
        File sources = new File("src/main");
        FileAlterationObserver observer = new FileAlterationObserver(sources, TrueFileFilter.INSTANCE);
        observer.addListener(new PipelineWatcher(this));
        watcher.addObserver(observer);
        try {
            mojo.getLog().info("Start watching " + sources.getAbsolutePath());
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
            try {
                if (watcher.accept(file)) {
                    if (! watcher.fileCreated(file)) {
                        break;
                    }
                }
            } catch (WatchingException e) {
                mojo.getLog().error("Watching exception: " + e.getMessage() + " (check log for more details)");
                break;
            }
        }
        mojo.getLog().info("");
        mojo.getLog().info("");
    }

    public void onFileChange(File file) {
        mojo.getLog().info("");
        mojo.getLog().info("The watcher has detected a changed file: " + file.getAbsolutePath());
        mojo.getLog().info("");
        for (Watcher w : watchers) {
            try {
                if (w.accept(file)) {
                    if (!w.fileUpdated(file)) {
                        break;
                    }
                }
            } catch (WatchingException e) {
                mojo.getLog().error("Watching exception: " + e.getMessage() + " (check log for more details)");
                break;
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
            try {
                if (watcher.accept(file)) {
                    if (!watcher.fileDeleted(file)) {
                        break;
                    }
                }
            } catch (WatchingException e) {
                mojo.getLog().error("Watching exception: " + e.getMessage() + " (check log for more details)");
                break;
            }
        }
        mojo.getLog().info("");
        mojo.getLog().info("");
    }
}
