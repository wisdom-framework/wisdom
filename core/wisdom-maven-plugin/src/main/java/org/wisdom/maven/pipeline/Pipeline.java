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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.plugin.Mojo;
import org.json.simple.JSONObject;
import org.wisdom.maven.Watcher;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.DefensiveThreadFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The pipeline is the spine of the watching system of Wisdom.
 * Each Mojo, i.e. Maven Plugin, willing to become a watcher, will be plugged to the pipeline. Registration is made
 * using org.wisdom.maven.pipeline.Watchers#add(org.apache.maven.execution.MavenSession, org.wisdom.maven.Watcher).
 * <p>
 * The pipeline is an internal class and should not be used directly. It just delegates the file events to the
 * watchers. So this class holds the file alteration monitor that triggers the reactions of the different mojos.
 */
public class Pipeline {

    public static final String EMPTY_STRING = "";
    private final boolean pomFileMonitoring;
    private List<Watcher> watchers = new ArrayList<>();
    private final Mojo mojo;
    private FileAlterationMonitor watcher;
    private final File baseDir;

    /**
     * The file used to store a JSON representation of Watching Exceptions happening on a watched event.
     */
    private File error;

    private static final String WATCHING_EXCEPTION_MESSAGE = "Watching exception: %s (check log for more details)";


    /**
     * Creates a new pipeline. Notice that the set of watchers cannot change.
     *  @param mojo    the 'run' mojo
     * @param baseDir the base directory of the watched project
     * @param list    the set of watchers plugged on the pipeline, the order of the list will be the notification order.
     * @param pomFileMonitoring flag enabling or disabling the pom file monitoring
     */
    public Pipeline(Mojo mojo, File baseDir, List<? extends Watcher> list, boolean pomFileMonitoring) {
        this.mojo = mojo;
        this.baseDir = baseDir;
        this.pomFileMonitoring = pomFileMonitoring;
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
     *
     * @return the current pipeline.
     */
    public Pipeline watch() {
        // Delete all error reports before starting the watcher.
        error = new File(baseDir, "target/pipeline");
        FileUtils.deleteQuietly(error);
        mojo.getLog().debug("Creating the target/pipeline directory : " + error.mkdirs());

        // Start the watching process.
        watcher = new FileAlterationMonitor(Integer.getInteger("watch.period", 2) * 1000);
        watcher.setThreadFactory(new DefensiveThreadFactory("wisdom-pipeline-watcher", mojo));
        FileAlterationObserver srcObserver = new FileAlterationObserver(new File(baseDir, "src"),
                TrueFileFilter.INSTANCE);
        PipelineWatcher listener = new PipelineWatcher(this);
        srcObserver.addListener(listener);
        watcher.addObserver(srcObserver);

        if (pomFileMonitoring) {
            FileAlterationObserver pomObserver = new FileAlterationObserver(baseDir, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.equals(new File(baseDir, "pom.xml"));
                }
            });
            pomObserver.addListener(listener);
            watcher.addObserver(pomObserver);
        }

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
     *
     * @param file the created file
     */
    public void onFileCreate(File file) {
        mojo.getLog().info(EMPTY_STRING);
        mojo.getLog().info("The watcher has detected a new file: " + file.getAbsolutePath());
        mojo.getLog().info(EMPTY_STRING);
        for (Watcher watcher : watchers) {
            if (watcher.accept(file)) {
                // This flag will be set to false if the processing must be interrupted.
                boolean continueProcessing;
                try {
                    cleanupErrorFile(watcher);
                    continueProcessing = watcher.fileCreated(file);
                } catch (WatchingException e) { //NOSONAR
                    mojo.getLog().debug(watcher + " has thrown an exception while handling the " + file.getName() + EMPTY_STRING +
                            " creation", e);
                    mojo.getLog().error(String.format(WATCHING_EXCEPTION_MESSAGE, e.getMessage()));
                    createErrorFile(watcher, e);
                    continueProcessing = false;
                }
                if (!continueProcessing) {
                    break;
                }
            }
        }
        mojo.getLog().info(EMPTY_STRING);
        mojo.getLog().info(EMPTY_STRING);
    }

    /**
     * Creates the error file storing the information from the given exception in JSON. This file is consumed by the
     * Wisdom server to generate an error page reporting the watching exception.
     *
     * @param watcher the watcher having thrown the exception
     * @param e       the exception
     */
    @SuppressWarnings("unchecked")
    private void createErrorFile(Watcher watcher, WatchingException e) {
        mojo.getLog().debug("Creating error file for '" + e.getMessage() + "' happening at " + e.getLine() + ":" + e
                .getCharacter() + " of " + e.getFile() + ", created by watcher : " + watcher);
        JSONObject obj = new JSONObject();
        obj.put("message", e.getMessage());
        if (watcher instanceof WatcherDelegate) {
            obj.put("watcher", ((WatcherDelegate) watcher).getDelegate().getClass().getName());
        } else {
            obj.put("watcher", watcher.getClass().getName());
        }
        if (e.getFile() != null) {
            obj.put("file", e.getFile().getAbsolutePath());
        }
        if (e.getLine() != -1) {
            obj.put("line", e.getLine());
        }
        if (e.getCharacter() != -1) {
            obj.put("character", e.getCharacter());
        }
        if (e.getCause() != null) {
            obj.put("cause", e.getCause().getMessage());
        }
        if (e.getTitle() != null) {
            obj.put("title", e.getTitle());
        }
        try {
            FileUtils.writeStringToFile(getErrorFileForWatcher(watcher), obj.toJSONString(), false);
        } catch (IOException e1) {
            mojo.getLog().error("Cannot write the error file", e1);
        }
    }

    /**
     * Method called on each event before the processing, deleting the error file is this file exists.
     *
     * @param watcher the watcher
     */
    private void cleanupErrorFile(Watcher watcher) {
        File file = getErrorFileForWatcher(watcher);
        FileUtils.deleteQuietly(file);
    }

    private File getErrorFileForWatcher(Watcher watcher) {
        if (watcher instanceof WatcherDelegate) {
            return new File(error, ((WatcherDelegate) watcher).getDelegate().toString() + ".json");
        } else {
            return new File(error, watcher + ".json");
        }
    }

    /**
     * The FAM has detected a change in a file. It dispatches this event to the watchers plugged on the current
     * pipeline.
     *
     * @param file the updated file
     */
    public void onFileChange(File file) {
        mojo.getLog().info(EMPTY_STRING);
        mojo.getLog().info("The watcher has detected a change in " + file.getAbsolutePath());
        mojo.getLog().info(EMPTY_STRING);
        for (Watcher watcher : watchers) {
            if (watcher.accept(file)) {
                cleanupErrorFile(watcher);
                // This flag will be set to false if the processing must be interrupted.
                boolean continueProcessing;
                try {
                    continueProcessing = watcher.fileUpdated(file);
                } catch (WatchingException e) { //NOSONAR
                    mojo.getLog().debug(watcher + " has thrown an exception while handling the " + file.getName() + EMPTY_STRING +
                            " update", e);
                    mojo.getLog().error(String.format(WATCHING_EXCEPTION_MESSAGE, e.getMessage()));
                    createErrorFile(watcher, e);
                    continueProcessing = false;
                }
                if (!continueProcessing) {
                    break;
                }
            }
        }
        mojo.getLog().info(EMPTY_STRING);
        mojo.getLog().info(EMPTY_STRING);
    }

    /**
     * The FAM has detected a file deletion. It dispatches this event to the watchers plugged on the current
     * pipeline.
     *
     * @param file the deleted file
     */
    public void onFileDelete(File file) {
        mojo.getLog().info(EMPTY_STRING);
        mojo.getLog().info("The watcher has detected a deleted file: " + file.getAbsolutePath());
        mojo.getLog().info(EMPTY_STRING);
        for (Watcher watcher : watchers) {
            if (watcher.accept(file)) {
                cleanupErrorFile(watcher);
                // This flag will be set to false if the processing must be interrupted.
                boolean continueProcessing;
                try {
                    continueProcessing = watcher.fileDeleted(file);
                } catch (WatchingException e) { //NOSONAR
                    mojo.getLog().debug(watcher + " has thrown an exception while handling the " + file.getName() + EMPTY_STRING +
                            " deletion", e);
                    mojo.getLog().error(String.format(WATCHING_EXCEPTION_MESSAGE, e.getMessage()));
                    createErrorFile(watcher, e);
                    continueProcessing = false;
                }
                if (!continueProcessing) {
                    break;
                }
            }
        }
        mojo.getLog().info(EMPTY_STRING);
        mojo.getLog().info(EMPTY_STRING);
    }
}
