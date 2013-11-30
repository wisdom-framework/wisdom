package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Launch the Wisdom Executor.
 */
public class WisdomExecutor {
    //TODO Extract this, because it's really error-prone.
    public static final String CHAMELEON_VERSION = "1.0.2-SNAPSHOT";

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

        Properties userProperties = mojo.session.getUserProperties();
        if (userProperties != null) {
            Enumeration<String> names = (Enumeration<String>) userProperties.propertyNames();
            while(names.hasMoreElements()) {
                String name = names.nextElement();
                cmdLine.addArgument("-D" + name + "=" + userProperties.getProperty(name));
            }
        }

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
            // Block execution until ctrl+c
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot execute Wisdom", e);
        }
    }
}
