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
package org.wisdom.maven.node;

import com.github.eirslett.maven.plugins.frontend.lib.TaskRunnerException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Manages an execution of NPM.
 */
public final class NPM {

    /**
     * The 'package.json' constant.
     */
    public static final String PACKAGE_JSON = "package.json";
    private final String npmName;
    private final String npmVersion;

    private final NodeManager node;
    private final Log log;
    private final String[] installArguments;

    private boolean handleQuoting = true;
    private LoggedOutputStream errorStreamFromLastExecution;
    private LoggedOutputStream outputStreamFromLastExecution;
    private boolean registerOutputStream = false;


    /**
     * Constructor used to install an NPM.
     *
     * @param log     the logger
     * @param manager the node manager
     * @param name    the NPM name
     * @param version the NPM version
     * @param args    additional installation arguments.
     */
    private NPM(Log log, NodeManager manager, String name, String version, String... args) {
        this.node = manager;
        this.npmName = name;
        this.npmVersion = version;
        this.log = log;
        this.handleQuoting = false;
        this.installArguments = args;
        ensureNodeInstalled();
    }

    /**
     * Let Wisdom handle the quoting of the arguments passed to an NPM execution.
     * Notice that generally, NPM executables handle the quoting by themselves, meaning that you should be careful
     * before using this method.
     *
     * @return the current NPM
     */
    public NPM handleQuoting() {
        handleQuoting = true;
        return this;
    }

    private void ensureNodeInstalled() {
        try {
            node.installIfNotInstalled();
        } catch (IOException e) {
            log.error("Cannot install node", e);
        }
    }

    /**
     * Executes the current NPM.
     * NPM can have several executable attached to them, so the 'binary' argument specifies which
     * one has to be executed. Check the 'bin' entry of the package.json file to determine which
     * one you need. 'Binary' is the key associated with the executable to invoke. For example, in
     * <code>
     * <pre>
     *      "bin": {
     *           "coffee": "./bin/coffee",
     *           "cake": "./bin/cake"
     *      },
     *     </pre>
     * </code>
     * <p/>
     * we have two alternatives: 'coffee' and 'cake'.
     *
     * @param binary the key of the binary to invoke
     * @param args   the arguments
     * @return the execution exit status
     * @throws MojoExecutionException if the execution failed
     */
    public int execute(String binary, String... args) throws MojoExecutionException {
        File destination = getNPMDirectory();
        if (!destination.isDirectory()) {
            throw new IllegalStateException("The npm module " + this.npmName + " is not installed");
        }

        CommandLine cmdLine = new CommandLine(node.getNodeExecutable());
        File npmExec = null;
        try {
            npmExec = findExecutable(binary);
        } catch (IOException | ParseException e) { //NOSONAR
            log.error(e);
        }
        if (npmExec == null) {
            throw new IllegalStateException("Cannot execute NPM " + this.npmName + " - cannot find the JavaScript file " +
                    "matching " + binary + " in the " + PACKAGE_JSON + " file");
        }

        // NPM is launched using the main file.
        cmdLine.addArgument(npmExec.getAbsolutePath(), false);
        for (String arg : args) {
            cmdLine.addArgument(arg, this.handleQuoting);
        }

        DefaultExecutor executor = new DefaultExecutor();

        executor.setExitValue(0);

        errorStreamFromLastExecution = new LoggedOutputStream(log, true, true);
        outputStreamFromLastExecution = new LoggedOutputStream(log, false, registerOutputStream);
        PumpStreamHandler streamHandler = new PumpStreamHandler(
                outputStreamFromLastExecution,
                errorStreamFromLastExecution);

        executor.setStreamHandler(streamHandler);
        executor.setWorkingDirectory(node.getWorkDir());
        log.info("Executing " + cmdLine.toString() + " from " + executor.getWorkingDirectory().getAbsolutePath());

        try {
            return executor.execute(cmdLine, extendEnvironmentWithNodeInPath(node));
        } catch (IOException e) {
            throw new MojoExecutionException("Error during the execution of the NPM " + npmName, e);
        }

    }

    private static Map<String, String> extendEnvironmentWithNodeInPath(NodeManager node) throws IOException {
        Map<String, String> env = EnvironmentUtils.getProcEnvironment();
        if (env.containsKey("PATH")) {
            String path = env.get("PATH");
            env.put("PATH", node.getNodeExecutable().getParent() + File.pathSeparator + path);
        } else {
            env.put("PATH", node.getNodeExecutable().getParent());
        }
        return env;
    }

    /**
     * Executes the current NPM using the given binary file.
     *
     * @param binary the program to run
     * @param args   the arguments
     * @return the execution exit status
     * @throws MojoExecutionException if the execution failed
     */
    public int execute(File binary, String... args) throws MojoExecutionException {
        File destination = getNPMDirectory();
        if (!destination.isDirectory()) {
            throw new IllegalStateException("NPM " + this.npmName + " not installed");
        }

        CommandLine cmdLine = new CommandLine(node.getNodeExecutable());

        if (binary == null) {
            throw new IllegalStateException("Cannot execute NPM " + this.npmName + " - the given binary is 'null'.");
        }

        if (!binary.isFile()) {
            throw new IllegalStateException("Cannot execute NPM " + this.npmName + " - the given binary does not " +
                    "exist: " + binary.getAbsoluteFile() + ".");
        }

        // NPM is launched using the main file.
        cmdLine.addArgument(binary.getAbsolutePath(), false);
        for (String arg : args) {
            cmdLine.addArgument(arg, this.handleQuoting);
        }

        DefaultExecutor executor = new DefaultExecutor();

        executor.setExitValue(0);

        errorStreamFromLastExecution = new LoggedOutputStream(log, true, true);
        outputStreamFromLastExecution = new LoggedOutputStream(log, false, registerOutputStream);

        PumpStreamHandler streamHandler = new PumpStreamHandler(
                outputStreamFromLastExecution,
                errorStreamFromLastExecution);

        executor.setStreamHandler(streamHandler);
        executor.setWorkingDirectory(node.getWorkDir());
        log.info("Executing " + cmdLine.toString() + " from " + executor.getWorkingDirectory().getAbsolutePath());

        try {
            return executor.execute(cmdLine, extendEnvironmentWithNodeInPath(node));
        } catch (IOException e) {
            throw new MojoExecutionException("Error during the execution of the NPM " + npmName, e);
        }

    }

    /**
     * Gets the error stream from the last NPM execution.
     *
     * @return the error stream.
     */
    public String getLastErrorStream() {
        if (errorStreamFromLastExecution != null) {
            return errorStreamFromLastExecution.getOutput();
        } else {
            return null;
        }
    }

    /**
     * Gets the output stream from the last NPM execution. The output stream must have been explicitly recorded
     * using {@link #registerOutputStream(boolean)}.
     *
     * @return the output stream.
     */
    public String getLastOutputStream() {
        if (outputStreamFromLastExecution != null) {
            return outputStreamFromLastExecution.getOutput();
        } else {
            return null;
        }
    }

    /**
     * Tries to find the main JS file.
     * This search is based on the `package.json` file and it's `bin` entry.
     * If there is an entry in the `bin` object matching `binary`, it uses this javascript file.
     * If the search failed, `null` is returned
     *
     * @return the JavaScript file to execute, null if not found
     */
    public File findExecutable(String binary) throws IOException, ParseException {
        File npmDirectory = getNPMDirectory();
        File packageFile = new File(npmDirectory, PACKAGE_JSON);
        if (!packageFile.isFile()) {
            throw new IllegalStateException("Invalid NPM " + npmName + " - " + packageFile.getAbsolutePath() + " does not" +
                    " exist");
        }
        FileReader reader = null;
        try {
            reader = new FileReader(packageFile);
            JSONObject json = (JSONObject) JSONValue.parseWithException(reader);
            JSONObject bin = (JSONObject) json.get("bin");
            if (bin == null) {
                log.error("No `bin` object in " + packageFile.getAbsolutePath());
                return null;
            } else {
                String exec = (String) bin.get(binary);
                if (exec == null) {
                    log.error("No `" + binary + "` object in the `bin` object from " + packageFile
                            .getAbsolutePath());
                    return null;
                }
                File file = new File(npmDirectory, exec);
                if (!file.isFile()) {
                    log.error("To execute " + npmName + ", an entry was found for " + binary + " in 'package.json', " +
                            "but the specified file does not exist - " + file.getAbsolutePath());
                    return null;
                }
                return file;
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private File getNPMDirectory() {
        return new File(node.getNodeModulesDirectory(), npmName);
    }

    /**
     * Installs the NPM. The NPM is installed using {@code npm install npm@version}, without the {@code -g} option.
     * The installation is executed from the installation directory of node.
     */
    private void install() {
        File directory = getNPMDirectory();
        if (directory.isDirectory()) {
            // Check the version
            String version = getVersionFromNPM(directory, log);
            // Are we looking for a specific version ?
            if (npmVersion != null) {
                // Yes
                if (!npmVersion.equals(version)) {
                    log.info("The NPM " + npmName + " is already installed but not in the requested version" +
                            " (requested: " + npmVersion + " - current: " + version + ") - uninstall it");
                    try {
                        FileUtils.deleteDirectory(directory);
                    } catch (IOException e) { //NOSONAR
                        // ignore it.
                    }
                } else {
                    log.debug("NPM " + npmName + " already installed in " + directory.getAbsolutePath() +
                            " (" + version + ")");
                    return;
                }
            } else {
                // No
                log.debug("NPM " + npmName + " already installed in " + directory.getAbsolutePath() + " " +
                        "(" + version + ")");
                return;
            }
        }

        StringBuilder command = new StringBuilder();
        command.append("install ");
        if (installArguments != null) {
            for (String s : installArguments) {
                command.append(s);
                command.append(" ");
            }
        }

        if (npmVersion != null) {
            command.append(npmName).append("@").append(npmVersion);
        } else {
            command.append(npmName);
        }

        try {
            node.factoryForNPMInstallation().getNpmRunner(node.proxy()).execute(command.toString());
        } catch (TaskRunnerException e) {
            log.error("Error during the installation of the NPM " + npmName + " - check log", e);
        }
    }

    /**
     * Utility method to extract the version from a NPM by reading its 'package.json' file.
     *
     * @param npmDirectory the directory in which the NPM is installed
     * @param log          the logger object
     * @return the read version, "0.0.0" if there are not 'package.json' file, {@code null} if this file cannot be
     * read or does not contain the "version" metadata
     */
    public static String getVersionFromNPM(File npmDirectory, Log log) {
        File packageFile = new File(npmDirectory, PACKAGE_JSON);
        if (!packageFile.isFile()) {
            return "0.0.0";
        }

        FileReader reader = null;
        try {
            reader = new FileReader(packageFile);  //NOSONAR
            JSONObject json = (JSONObject) JSONValue.parseWithException(reader);
            return (String) json.get("version");
        } catch (IOException | ParseException e) {
            log.error("Cannot extract version from " + packageFile.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        return null;
    }

    /**
     * Creates an NPM object based on the NPM's name and version (or tag).
     * If the NPM is not installed, it installs it.
     * There returned NPM let you execute it.
     *
     * @param mojo    the Wisdom Mojo
     * @param name    the NPM name
     * @param version the NPM version or tag
     * @param args    additional arguments (use with precautions)
     * @return the NPM object. The NPM may have been installed if it was not installed or installed in another version.
     */
    public static NPM npm(AbstractWisdomMojo mojo, String name, String version, String... args) {
        NPM npm = new NPM(mojo.getLog(), mojo.getNodeManager(), name, version, args);
        npm.install();
        return npm;
    }

    /**
     * Configures the NPM registry location.
     *
     * @param node           the node manager
     * @param log            the logger
     * @param npmRegistryUrl the registry url
     */
    public static void configureRegistry(NodeManager node, Log log, String npmRegistryUrl) {
        try {
            node.factory().getNpmRunner(node.proxy()).execute("config set registry " + npmRegistryUrl);
        } catch (TaskRunnerException e) {
            log.error("Error during the configuration of NPM registry with the url " + npmRegistryUrl + " - check log", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NPM npm = (NPM) o;

        return npmName.equals(npm.npmName)
                && !(npmVersion != null ? !npmVersion.equals(npm.npmVersion) : npm.npmVersion != null);

    }

    @Override
    public int hashCode() {
        int result = npmName.hashCode();
        result = 31 * result + (npmVersion != null ? npmVersion.hashCode() : 0);
        return result;
    }

    /**
     * Enables the recoding ot the output stream when the current NPM is executed.
     *
     * @param register whether or not the output stream must be recorded
     */
    public void registerOutputStream(boolean register) {
        registerOutputStream = register;
    }
}
