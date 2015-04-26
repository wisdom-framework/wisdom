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
package org.wisdom.maven.utils;

import com.google.common.base.Strings;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Launch the Wisdom Framework.
 * This class starts the Wisdom Framework. It handles background (start and stop) and foreground executions.
 */
public class WisdomExecutor {

    /**
     * Retrieves the version of Chameleon we use.
     */
    public static final String CHAMELEON_VERSION = BuildConstants.get("CHAMELEON_VERSION");

    /**
     * The maximum time to wait in ms for file creation and deletion.
     */
    public static final int FILE_WAIT_TIMEOUT = 10000;

    /**
     * Launches the Wisdom server in background using the {@literal chameleon.sh} scripts.
     * This method works only on Linux and Mac OS X.
     *
     * @param mojo the mojo
     * @throws MojoExecutionException if the Wisdom instance cannot be started.
     */
    public void executeInBackground(AbstractWisdomMojo mojo) throws MojoExecutionException {
        File script = new File(mojo.getWisdomRootDirectory(), "chameleon.sh");
        if (!script.isFile()) {
            throw new MojoExecutionException("The 'chameleon.sh' file does not exist in " + mojo
                    .getWisdomRootDirectory().getAbsolutePath() + " - cannot launch Wisdom");
        }

        // Remove the RUNNING_PID file if exists.
        File pid = new File(mojo.getWisdomRootDirectory(), "RUNNING_PID");
        if (pid.isFile()) {
            mojo.getLog().info("The RUNNING_PID file is present, deleting it");
            FileUtils.deleteQuietly(pid);
        }

        // We create a command line, as we are using th toString method on it.
        CommandLine cmdLine = new CommandLine(script);
        appendSystemPropertiesToCommandLine(mojo, cmdLine);

        try {
            mojo.getLog().info("Launching Wisdom Server using '" + cmdLine.toString() + "'.");
            // As we know which command line we are executing, we can safely execute the command.
            Runtime.getRuntime().exec(cmdLine.toStrings(), null, mojo.getWisdomRootDirectory()); //NOSONAR see comment
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot execute Wisdom", e);
        }
    }

    /**
     * Launches the Wisdom server. This method blocks until the wisdom server shuts down.
     * It uses the {@literal Java} executable directly.
     *
     * @param mojo        the mojo
     * @param interactive enables the shell prompt
     * @param debug       the debug port (0 to disable it)
     * @param jvmArgs     JVM arguments to add to the `java` command (before the -jar argument).
     * @param destroyer   a process destroyer that can be used to destroy the process, if {@code null}
     *                    a {@link org.apache.commons.exec.ShutdownHookProcessDestroyer} is used.
     * @throws MojoExecutionException if the Wisdom instance cannot be started or has thrown an unexpected status
     *                                while being stopped.
     */
    public void execute(AbstractWisdomMojo mojo, boolean interactive, int debug, String jvmArgs,
                        ProcessDestroyer destroyer) throws MojoExecutionException {
        // Get java
        File java = ExecUtils.find("java", new File(mojo.javaHome, "bin"));
        if (java == null) {
            throw new MojoExecutionException("Cannot find the java executable");
        }

        CommandLine cmdLine = new CommandLine(java);

        if (debug != 0) {
            cmdLine.addArgument(
                    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debug,
                    false);
        }

        if (!Strings.isNullOrEmpty(jvmArgs)) {
            cmdLine.addArguments(jvmArgs, false);
        }

        cmdLine.addArgument("-jar");
        cmdLine.addArgument("bin/chameleon-core-" + CHAMELEON_VERSION + ".jar");
        if (interactive) {
            cmdLine.addArgument("--interactive");
        }

        appendSystemPropertiesToCommandLine(mojo, cmdLine);

        DefaultExecutor executor = new DefaultExecutor();
        if (destroyer != null) {
            executor.setProcessDestroyer(destroyer);
        } else {
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        }

        executor.setWorkingDirectory(mojo.getWisdomRootDirectory());
        if (interactive) {
            executor.setStreamHandler(new PumpStreamHandler(System.out, System.err, System.in)); //NOSONAR
            // Using the interactive mode the framework should be stopped using the 'exit' command,
            // and produce a '0' status.
            executor.setExitValue(0);
        } else {
            executor.setStreamHandler(new PumpStreamHandler(System.out, System.err)); // NOSONAR
            // As the execution is intended to be interrupted using CTRL+C, the status code returned is expected to be 1
            // 137 or 143 is used when stopped by the destroyer.
            executor.setExitValues(new int[]{1, 137, 143});
        }
        try {
            mojo.getLog().info("Launching Wisdom Server");
            mojo.getLog().debug("Command Line: " + cmdLine.toString());
            // The message is different whether or not we are in the interactive mode.
            if (interactive) {
                mojo.getLog().info("You are in interactive mode");
                mojo.getLog().info("Hit 'exit' to shutdown");
            } else {
                mojo.getLog().info("Hit CTRL+C to exit");
            }
            if (debug != 0) {
                mojo.getLog().info("Wisdom launched with remote debugger interface enabled on port " + debug);
            }
            // Block execution until ctrl+c
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot execute Wisdom", e);
        }
    }

    /**
     * Appends the properties from the Maven session (user properties) to the command line. As the command line is
     * intended to be a Chameleon process, arguments are passed using the {@literal -Dkey=value} syntax.
     *
     * @param mojo the mojo
     * @param cmd  the command line to extend
     */
    private static void appendSystemPropertiesToCommandLine(AbstractWisdomMojo mojo, CommandLine cmd) {
        Properties userProperties = mojo.session.getUserProperties();
        if (userProperties != null) {
            //noinspection unchecked
            Enumeration<String> names = (Enumeration<String>) userProperties.propertyNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                cmd.addArgument("-D" + name + "=" + userProperties.getProperty(name));
            }
        }
    }

    /**
     * Stops a running instance of wisdom using 'chameleon stop'.
     *
     * @param mojo the mojo
     * @throws MojoExecutionException if the instance cannot be stopped
     */
    public void stop(AbstractWisdomMojo mojo) throws MojoExecutionException {
        File script = new File(mojo.getWisdomRootDirectory(), "chameleon.sh");
        if (!script.isFile()) {
            throw new MojoExecutionException("The 'chameleon.sh' file does not exist in " + mojo
                    .getWisdomRootDirectory().getAbsolutePath() + " - cannot stop the Wisdom instance");
        }

        // If there is a RUNNING_PID file, exit immediately.
        File pid = new File(mojo.getWisdomRootDirectory(), "RUNNING_PID");
        if (!pid.isFile()) {
            mojo.getLog().info("The RUNNING_PID file does not exist, are you sure Wisdom is running ?");
            return;
        }

        CommandLine cmdLine = new CommandLine(script);
        cmdLine.addArgument("stop");

        try {
            mojo.getLog().info("Stopping Wisdom Server using '" + cmdLine.toString() + "'.");
            // As we know which command line we are executing, we can safely execute the command.
            Runtime.getRuntime().exec(cmdLine.toStrings(), null, mojo.getWisdomRootDirectory()); //NOSONAR
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot stop Wisdom", e);
        }
    }

    /**
     * Waits for a file to be created.
     *
     * @param file the file
     * @return {@literal true} if the file was created, {@literal false} otherwise
     */
    public static boolean waitForFile(File file) {
        if (file.isFile()) {
            return true;
        } else {
            // Start waiting 10 seconds maximum
            long timeout = System.currentTimeMillis() + FILE_WAIT_TIMEOUT;
            while (System.currentTimeMillis() <= timeout) {
                sleepQuietly(10);
                if (file.isFile()) {
                    return true;
                }
            }
        }
        // Timeout reached
        return false;
    }

    /**
     * Sleeps for the given amount of time.
     *
     * @param time the time
     */
    private static void sleepQuietly(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // Ignore it.
        }
    }

    /**
     * Wiats for a file to be deleted.
     *
     * @param file the file
     * @return {@literal true} if the file was deleted, {@literal false} otherwise
     */
    public static boolean waitForFileDeletion(File file) {
        if (!file.isFile()) {
            return true;
        } else {
            // Start waiting 10 seconds maximum
            long timeout = System.currentTimeMillis() + FILE_WAIT_TIMEOUT;
            while (System.currentTimeMillis() <= timeout) {
                sleepQuietly(10);
                if (!file.isFile()) {
                    return true;
                }
            }
        }
        // Timeout reached
        return false;
    }

}
