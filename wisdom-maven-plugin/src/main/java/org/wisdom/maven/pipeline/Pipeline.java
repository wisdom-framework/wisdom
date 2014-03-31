/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
 * The pipeline is the spine of the watching system of Wisdom.
 * Each Mojo, i.e. Maven Plugin, willing to become a watcher, will be plugged to the pipeline. Registration is made
 * using org.wisdom.maven.pipeline.Watchers#add(org.apache.maven.execution.MavenSession, org.wisdom.maven.Watcher).
 * <p/>
 * The pipeline is an internal class and should not be used directly. It just delegates the file events to the
 * watchers. So this class holds the file alteration monitor that triggers the reactions of the different mojos.
 */
public class Pipeline {

    private List<Watcher> watchers = new ArrayList<>();
    private final Mojo mojo;
    private FileAlterationMonitor watcher;
    private final File baseDir;

    private final static String WATCHING_EXCEPTION_MESSAGE = "Watching exception: %s (check log for more details)";


    /**
     * Creates a new pipeline. Notice that the set of watchers cannot change.
     * @param mojo the 'run' mojo
     * @param baseDir the base directory of the watched project
     * @param list the set of watchers plugged on the pipeline, the order of the list will be the notification order.
     */
    public Pipeline(Mojo mojo, File baseDir, List<? extends Watcher> list) {
        this.mojo = mojo;
        this.baseDir = baseDir;
        mojo.getLog().debug("Initializing watch mode with " + list);
        watchers = new ArrayList<>();
        for (Object o : list) {
            watchers.add(new WatcherDelegate(o));
        }
    }

    /**
     * Shuts down the pipeline. This methods stops the FAM.
     */
    public void shutdown() {
        try {
            watcher.stop();
        } catch (Exception e) { //NOSONAR
            mojo.getLog().debug("Something went terribly wrong when we try to stopped the FAM of the pipeline", e);
            // ignore it.
        }
    }

    /**
     * Starts the watching.
     * @return the current pipeline.
     */
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

    /**
     * The FAM has detected a new file. It dispatches this event to the watchers plugged on the current pipeline.
     * @param file the created file
     */
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
                    mojo.getLog().debug(watcher + " has thrown an exception while handling the " + file.getName() + "" +
                            " creation", e);
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

    /**
     * The FAM has detected a change in a file. It dispatches this event to the watchers plugged on the current
     * pipeline.
     * @param file the updated file
     */
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
                    mojo.getLog().debug(watcher + " has thrown an exception while handling the " + file.getName() + "" +
                            " update", e);
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

    /**
     * The FAM has detected a file deletion. It dispatches this event to the watchers plugged on the current
     * pipeline.
     * @param file the deleted file
     */
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
                    mojo.getLog().debug(watcher + " has thrown an exception while handling the " + file.getName() + "" +
                            " deletion", e);
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
