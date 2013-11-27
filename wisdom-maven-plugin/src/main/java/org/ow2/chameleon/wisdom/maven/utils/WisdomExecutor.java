package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.IOException;

/**
 * Launch the Wisdom Executor.
 */
public class WisdomExecutor {
    public static final String CHAMELEON_VERSION = "1.0.1";

    public void execute(AbstractWisdomMojo mojo) throws MojoExecutionException {
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

        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWorkingDirectory(mojo.getWisdomRootDirectory());
        executor.setStreamHandler(new PumpStreamHandler());
        try {
            mojo.getLog().info("Launching Wisdom Server");
            mojo.getLog().info("Hit CTRL+C to exit");
            if (mojo.debug != 0) {
                mojo.getLog().info("Wisdom launched with remote debugger interface enabled on port " + mojo.debug);
            }
            System.out.println(cmdLine);
            // Block execution until ctrl+c
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot execute Wisdom", e);
        }
    }
}
