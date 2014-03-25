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

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.wisdom.maven.Constants;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.utils.ExecUtils;
import org.wisdom.maven.utils.PlexusLoggerWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A class managing node and npm
 * Must be sure that .npmrc file does not set the prefix.
 */
public class NodeManager {

    public static final String NODE_DIST = "http://nodejs.org/dist/v";
    public static final String NPM_DIST = "http://nodejs.org/dist/npm/npm-";
    private final Log log;
    private final File nodeDirectory;
    private final File npmDirectory;
    private final File nodeModulesDirectory;
    private final File nodeLibDirectory;
    private File nodeExecutable;

    public NodeManager(AbstractWisdomMojo mojo) {
        this(mojo.getLog(),
                new File(System.getProperty("user.home"), ".wisdom/node/" + Constants.NODE_VERSION));
    }

    public NodeManager(Log log, File nodeDirectory) {
        this.nodeDirectory = nodeDirectory;
        this.npmDirectory = new File(nodeDirectory, "lib/node_modules/npm/");

        if (!nodeDirectory.exists()) {
            nodeDirectory.mkdirs();
        }

        this.log = log;
        if (ExecUtils.isWindows()) {
            this.nodeExecutable = new File(nodeDirectory + "/bin", "node.exe");
            nodeLibDirectory = nodeExecutable.getParentFile();
            nodeLibDirectory.mkdirs();
        } else {
            this.nodeExecutable = new File(nodeDirectory + "/bin", "node");
            File nodePrefix = nodeExecutable.getParentFile().getParentFile();
            nodeLibDirectory = new File(nodePrefix, "lib");
            nodeLibDirectory.mkdirs();
        }

        nodeModulesDirectory = new File(nodeLibDirectory, "node_modules");
    }

    /**
     * Installs node in ~/.wisdom/node/$version.
     * The installation process is the following:
     * <ol>
     * <li>download node</li>
     * <li>expand node to the right location</li>
     * <li>download npm</li>
     * <li>expand npm</li>
     * </ol>
     * <p/>
     * Node and npm installation are divided to avoid facing npm corruption on node package.
     *
     * @throws java.io.IOException
     */
    public void installIfNotInstalled() throws IOException {
        if (!nodeExecutable.isFile()) {
            downloadAndInstallNode();
            downloadAndInstallNPM();
        } else {
            log.debug("Node executable : " + nodeExecutable.getAbsolutePath());
        }
    }

    public File getNodeExecutable() {
        return nodeExecutable;
    }

    public File getNodeModulesDirectory() {
        return nodeModulesDirectory;
    }

    private void downloadAndInstallNPM() throws IOException {
        URL url = new URL(NPM_DIST + Constants.NPM_VERSION + ".zip");
        File tmp = File.createTempFile("npm", ".zip");

        log.info("Downloading npm-" + Constants.NPM_VERSION + " from " + url.toExternalForm());
        FileUtils.copyURLToFile(url, tmp);
        log.info("npm downloaded - " + tmp.length() + " bytes");

        final ZipUnArchiver ua = new ZipUnArchiver();
        ua.enableLogging(new PlexusLoggerWrapper(log));
        ua.setOverwrite(true);
        ua.setSourceFile(tmp);
        ua.setDestDirectory(nodeLibDirectory);
        log.info("Unzipping npm");
        try {
            ua.extract();
        } catch (ArchiverException e) {
            log.error("Cannot unzip NPM", e);
            throw new IOException(e);
        }

    }

    private void downloadAndInstallNode() throws IOException {
        URL url;
        String path;
        if (ExecUtils.isWindows()) {
            if (ExecUtils.is64bit()) {
                url = new URL(NODE_DIST + Constants.NODE_VERSION + "/x64/node.exe");
            } else {
                url = new URL(NODE_DIST + Constants.NODE_VERSION + "/node.exe");
            }
            // Manage download for windows.
            log.info("Downloading nodejs from " + url.toExternalForm());

            // Create the bin directory
            File bin = new File(nodeDirectory, "bin");
            bin.mkdirs();

            FileUtils.copyURLToFile(url, nodeExecutable);
            log.info(nodeExecutable.getAbsolutePath() + " was downloaded from " + url.toExternalForm());
            // Try to set the file executable.
            nodeExecutable.setExecutable(true);

            return;
        } else if (ExecUtils.isMac()) {
            if (!ExecUtils.is64bit()) {
                path = "node-v" + Constants.NODE_VERSION + "-darwin-x86";
                url = new URL(NODE_DIST + Constants.NODE_VERSION + "/node-v" + Constants.NODE_VERSION + "-darwin-x86" +
                        ".tar.gz");
            } else {
                path = "node-v" + Constants.NODE_VERSION + "-darwin-x64";
                url = new URL(NODE_DIST + Constants.NODE_VERSION + "/node-v" + Constants.NODE_VERSION + "-darwin-x64" +
                        ".tar.gz");
            }
        } else if (ExecUtils.isLinux()) {
            if (!ExecUtils.is64bit()) {
                path = "node-v" + Constants.NODE_VERSION + "-linux-x86";
                url = new URL(NODE_DIST + Constants.NODE_VERSION + "/node-v" + Constants.NODE_VERSION + "-linux-x86.tar.gz");
            } else {
                path = "node-v" + Constants.NODE_VERSION + "-linux-x64";
                url = new URL(NODE_DIST + Constants.NODE_VERSION + "/node-v" + Constants.NODE_VERSION + "-linux-x64.tar.gz");
            }
        } else {
            throw new UnsupportedOperationException("Operating system `" + System.getProperty("os.name") + "` not " +
                    "supported");
        }

        File tmp = File.createTempFile("nodejs", ".tar.gz");
        log.info("Downloading nodejs-" + Constants.NODE_VERSION + " from " + url.toExternalForm());
        FileUtils.copyURLToFile(url, tmp);
        log.info("nodejs downloaded - " + tmp.length() + " bytes");

        File tmpDir = Files.createTempDir();
        tmpDir.mkdirs();

        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(new PlexusLoggerWrapper(log));
        ua.setOverwrite(true);
        ua.setSourceFile(tmp);
        ua.setDestDirectory(tmpDir);
        log.info("Expanding nodejs");
        try {
            ua.extract();
        } catch (ArchiverException e) {
            log.error("Cannot unzip node.js", e);
            throw new IOException(e);
        }

        // Move files
        File test = new File(tmpDir, path);
        if (!test.isDirectory()) {
            throw new IllegalStateException("Cannot find expanded directory " + test.getAbsolutePath());
        }

        FileUtils.copyDirectory(test, nodeDirectory);

        // Check node executable
        if (!nodeExecutable.isFile()) {
            throw new IllegalStateException("Node executable not found after installation");
        } else {
            nodeExecutable.setExecutable(true);
        }

        // Delete the installed npm if any
        if (npmDirectory.isDirectory()) {
            FileUtils.deleteDirectory(npmDirectory);
        }

    }

}
