package org.wisdom.maven.node;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Manage an execution of NPM
 */
public class NPM {

    private final String npmName;
    private final String npmVersion;

    private final NodeManager node;
    private final AbstractWisdomMojo mojo;

    private final String exec;
    private final String[] args;
    private boolean handleQuoting = true;

    /**
     * Constructor used for installation.
     * @param mojo
     * @param name
     * @param version
     */
    private NPM(AbstractWisdomMojo mojo, String name, String version) {
        this.node = mojo.node;
        this.npmName = name;
        this.npmVersion = version;
        this.mojo = mojo;
        this.args = new String[0];
        this.exec = this.npmName;
        ensureNodeInstalled();
    }

    /**
     * Constructor used for execution.
     * @param mojo
     * @param name
     * @param exec
     * @param args
     */
    private NPM(AbstractWisdomMojo mojo, String name, String exec, String... args) {
        this.node = mojo.node;
        this.npmName = name;
        this.npmVersion = null;
        this.mojo = mojo;
        this.args = args;
        if (exec != null) {
            this.exec = exec;
        } else {
            this.exec = this.npmName;
        }
        ensureNodeInstalled();
    }

    public void setHandleQuoting(boolean h) {
        handleQuoting = h;
    }

    private void ensureNodeInstalled() {
        try {
            mojo.node.installIfNotInstalled();
        } catch (IOException e) {
            mojo.getLog().error("Cannot install node", e);
        }
    }

    public void exec() throws MojoExecutionException {
        File destination = getNPMDirectory();
        if (! destination.isDirectory()) {
            throw new IllegalStateException("NPM " + this.npmName + " not installed");
        }

        CommandLine cmdLine = CommandLine.parse(node.getNodeExecutable().getAbsolutePath());
        File npmExec = null;
        try {
            npmExec = findExecutable();
        } catch (IOException | ParseException e) {
            mojo.getLog().error(e);
        }
        if (npmExec == null) {
            throw new IllegalStateException("Cannot execute NPM " + this.npmName + " - cannot find the JavaScript file " +
                    "matching " + this.exec + " in the package.json file");
        }

        cmdLine.addArgument(npmExec.getAbsolutePath()); // NPM is launched using the main file.
        for (String arg : this.args) {
            cmdLine.addArgument(arg, this.handleQuoting);
        }

        DefaultExecutor executor = new DefaultExecutor();

        executor.setExitValue(0);

        final OutputStream out = System.out;
        final OutputStream err = System.err;

        // Takes System.out for dumping the output and System.err for Error
        PumpStreamHandler streamHandler = new PumpStreamHandler(out, err);
        executor.setStreamHandler(streamHandler);
        mojo.getLog().info("Executing " + cmdLine.toString());

        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            mojo.getLog().error("Error during the execution of the NPM " + npmName + " - check log");
            throw new MojoExecutionException(e.getMessage());
        }

    }

    /**
     * Try to find the main JS file.
     * This search is based on the `package.json` file and it's `bin` entry.
     * If there is an entry in the `bin` object matching `exec`, it use this javascript file.
     * If the search failed, `null` is returned
     * @return the JavaScript file to execute, null if not found
     */
    private File findExecutable() throws IOException, ParseException {
        File npmDirectory = getNPMDirectory();
        File packageFile = new File(npmDirectory, "package.json");
        if (! packageFile.isFile()) {
            throw new IllegalStateException("Invalid NPM " + npmName + " - " + packageFile.getAbsolutePath() + " does not" +
                    " exist");
        }
        JSONObject json = (JSONObject) JSONValue.parseWithException(new FileReader(packageFile));
        JSONObject bin = (JSONObject) json.get("bin");
        if (bin == null) {
            mojo.getLog().error("No `bin` object in " + packageFile.getAbsolutePath());
            return null;
        } else {
            String exec = (String) bin.get(this.exec);
            if (exec == null) {
                mojo.getLog().error("No `" + this.exec + "` object in the `bin` object from " + packageFile
                        .getAbsolutePath());
                return null;
            }
            File file = new File(npmDirectory, exec);
            if (! file.isFile()) {
                mojo.getLog().error("A matching javascript file was found for " + this.exec + " but the file does " +
                        "not exist - " + file.getAbsolutePath());
                return null;
            }
            return file;
        }

    }

    private File getNPMDirectory() {
        return new File(node.getNodeModulesDirectory(), npmName);
    }

    public void install() {
        File destination = getNPMDirectory();
        if (destination.isDirectory()) {
            // Check the version
            String version = getVersionFromNPM(destination);
            // Are we looking for a specific version ?
            if (npmVersion != null) {
                // Yes
                if (! npmVersion.equals(version)) {
                    mojo.getLog().info("The NPM " + npmName + " is already installed but not in the requested version" +
                            " (requested: " + npmVersion + " - current: " + version + ") - uninstall it");
                    try {
                        FileUtils.deleteDirectory(destination);
                    } catch (IOException e) {
                        // ignore it.
                    }
                } else {
                    mojo.getLog().debug("NPM " + npmName + " already installed in " + destination.getAbsolutePath() +
                            " (" + version + ")");
                    return;
                }
            } else {
                // No
                mojo.getLog().debug("NPM " + npmName + " already installed in " + destination.getAbsolutePath() + " " +
                        "(" + version + ")");
                return;
            }
        }

        CommandLine cmdLine = CommandLine.parse(node.getNodeExecutable().getAbsolutePath());
        File npmCli = new File(node.getNodeModulesDirectory(), "npm/bin/npm-cli.js");
        cmdLine.addArgument(npmCli.getAbsolutePath()); // NPM is launched using the main file.
        cmdLine.addArgument("install");
        cmdLine.addArgument("-g");
        if (npmVersion != null) {
            cmdLine.addArgument(npmName + "@" + npmVersion);
        } else {
            cmdLine.addArgument(npmName);
        }

        DefaultExecutor executor = new DefaultExecutor();

        //executor.setWorkingDirectory(mojo.project.getBasedir());
        executor.setExitValue(0);

        final OutputStream out = System.out;
        final OutputStream err = System.err;

        // Takes System.out for dumping the output and System.err for Error
        PumpStreamHandler streamHandler = new PumpStreamHandler(out, err);
        executor.setStreamHandler(streamHandler);

        mojo.getLog().info("Executing " + cmdLine.toString());

        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            mojo.getLog().error("Error during the installation of the NPM " + npmName + " - check log");
        }
    }

    private String getVersionFromNPM(File destination) {
        File packageFile = new File(destination, "package.json");
        if (! packageFile.isFile()) {
            return "0.0.0";
        }

        try {
            JSONObject json = (JSONObject) JSONValue.parseWithException(new FileReader(packageFile));
            return (String) json.get("version");
        } catch (IOException | ParseException e) {
            mojo.getLog().error("Cannot extract version from " + packageFile.getAbsolutePath(), e);
        }

        return null;
    }

    public static class Install {

        private String name;
        private String version;
        private AbstractWisdomMojo mojo;

        public Install(AbstractWisdomMojo mojo) {
            this.mojo = mojo;
        }

        private void _install() {
            NPM npm = new NPM(mojo, name, version);
            npm.install();
        }

        public void install(URL url) {
            this.name = url.toExternalForm();
            _install();
        }

        public void install(File file) {
            this.name = file.getAbsolutePath();
            _install();
        }

        public void install(String name, String tagOrVersion) {
            this.name = name;
            this.version = tagOrVersion;
            _install();
        }

        public void install(String name) {
            this.name = name;
            _install();
        }

    }

    public static class Execution {

        private String name;
        private String exec;
        private AbstractWisdomMojo mojo;
        private String[] args;
        private boolean quoting = true;

        public Execution(AbstractWisdomMojo mojo) {
            this.mojo = mojo;
        }

        public Execution npm(String npm) {
            this.name = npm;
            return this;
        }

        public Execution command(String exec) {
            this.exec = exec;
            return this;
        }

        public Execution args(String... args) {
            this.args = args;
            return this;
        }

        public Execution withoutQuoting() {
            quoting = false;
            return this;
        }


        public void execute() throws MojoExecutionException {
            if (name == null) {
                throw new NullPointerException("npm name not set");
            }
            if (exec == null) {
                exec = name;
            }
            NPM npm = new NPM(mojo, name, exec, args);
            if (! quoting) {
                npm.setHandleQuoting(quoting);
            }
            npm.exec();
        }
    }

}
