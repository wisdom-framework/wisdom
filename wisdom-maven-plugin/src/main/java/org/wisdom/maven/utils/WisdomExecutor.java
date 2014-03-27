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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Launch the Wisdom Executor.
 */
public class WisdomExecutor {
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
     * @throws MojoExecutionException
     */
    public void executeInBackground(AbstractWisdomMojo mojo) throws MojoExecutionException {
        File script = new File(mojo.getWisdomRootDirectory(), "chameleon.sh");
        if (!script.isFile()) {
            throw new MojoExecutionException("The 'chameleon.sh' file does not exist in " + mojo
                    .getWisdomRootDirectory().getAbsolutePath() + " - cannot launch Wisdom");
        }

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
            Runtime.getRuntime().exec(cmdLine.toStrings(), null, mojo.getWisdomRootDirectory());
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
     * @throws MojoExecutionException
     */
    public void execute(AbstractWisdomMojo mojo, boolean interactive) throws MojoExecutionException {
        // Get java
        File java = ExecutableFinder.find("java", new File(mojo.javaHome, "bin"));
        if (java == null) {
            throw new MojoExecutionException("Cannot find the java executable");
        }

        CommandLine cmdLine = new CommandLine(java);

        if (mojo.debug != 0) {
            cmdLine.addArgument(
                    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + mojo.debug,
                    false);
        }

        cmdLine.addArgument("-jar");
        cmdLine.addArgument("bin/chameleon-core-" + CHAMELEON_VERSION + ".jar");
        if (interactive) {
            cmdLine.addArgument("--interactive");
        }

        appendSystemPropertiesToCommandLine(mojo, cmdLine);

        DefaultExecutor executor = new DefaultExecutor();


        executor.setWorkingDirectory(mojo.getWisdomRootDirectory());
        if (interactive) {
            executor.setStreamHandler(new PumpStreamHandler(System.out, System.err, System.in));
            // Using the interactive mode the framework should be stopped using the 'exit' command,
            // and produce a '0' status.
            executor.setExitValue(0);
        } else {
            executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
            // As the execution is intended to be interrupted using CTRL+C, the status code returned is expected to be 1
            executor.setExitValue(1);
        }
        try {
            mojo.getLog().info("Launching Wisdom Server");
            if (interactive) {
                mojo.getLog().info("You are in interactive mode");
                mojo.getLog().info("Hit 'exit' to shutdown");
            } else {
                mojo.getLog().info("Hit CTRL+C to exit");
            }
            if (mojo.debug != 0) {
                mojo.getLog().info("Wisdom launched with remote debugger interface enabled on port " + mojo.debug);
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
     * Stops a running instance of wisdom using 'chameleon stop'
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

        File pid = new File(mojo.getWisdomRootDirectory(), "RUNNING_PID");
        if (!pid.isFile()) {
            mojo.getLog().info("The RUNNING_PID file does not exist, are you sure Wisdom is running ?");
            return;
        }

        CommandLine cmdLine = new CommandLine(script);
        cmdLine.addArgument("stop");

        try {
            mojo.getLog().info("Stopping Wisdom Server using '" + cmdLine.toString() + "'.");
            Runtime.getRuntime().exec(cmdLine.toStrings(), null, mojo.getWisdomRootDirectory());
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
