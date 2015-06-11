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

    private static final String NODE_VERSION_PREFIX = "v";
    private static final String NPM_VERSION_PATTERN = "npm/-/npm-%1$s.tgz";
    private final Log log;
    private final File nodeDirectory;
    private final File npmDirectory;
    private final File nodeModulesDirectory;
    private final File nodeLibDirectory;
    private final AbstractWisdomMojo mojo;
    private File nodeExecutable;
	private String nodeDist;
	private String npmDist;

    private static final String nodeVersion;

    static {
        if (ExecUtils.isARM()) {
            nodeVersion = Constants.NODE_VERSION_ARM;
        } else {
            nodeVersion = Constants.NODE_VERSION;
        }
    }

    public NodeManager(AbstractWisdomMojo mojo) {
        this(mojo.getLog(), new File(System.getProperty("user.home"), ".wisdom/node/" + NodeManager.nodeVersion), mojo);
    }

    public NodeManager(Log log, File nodeDirectory, AbstractWisdomMojo mojo) {
        this.nodeDirectory = nodeDirectory;
        this.npmDirectory = new File(nodeDirectory, "lib/node_modules/npm/");
        this.mojo = mojo;

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
     * </ol>
     * <p>
     *
     * @throws java.io.IOException
     */
    public void installIfNotInstalled() throws IOException {
        if (!nodeExecutable.isFile()) {
            downloadAndInstallNode();
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

    private String getNodeDist() {
        if (this.nodeDist == null) {
            String configUrl = mojo.getNodeDistributionRootUrl();
    		this.nodeDist = configUrl + (configUrl.endsWith("/")?"":"/") + NODE_VERSION_PREFIX;
        }
        return this.nodeDist;
    }
    
    private String getNpmDist() {
        if (this.npmDist == null) {
            String configUrl = mojo.getNpmRegistryRootUrl();
            this.npmDist = configUrl + (configUrl.endsWith("/")?"":"/") + NPM_VERSION_PATTERN;
        }
		return this.npmDist;
    }
    
    private void downloadAndInstallNPM() throws IOException {
        URL url = new URL(String.format(getNpmDist(), Constants.NPM_VERSION));
        File tmp = File.createTempFile("npm", ".tgz");

        log.info("Downloading npm-" + Constants.NPM_VERSION + " from " + url.toExternalForm());
        FileUtils.copyURLToFile(url, tmp);
        log.info("npm downloaded - " + tmp.length() + " bytes");

        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(new PlexusLoggerWrapper(log));
        ua.setOverwrite(true);
        ua.setSourceFile(tmp);
        final File modules = new File(nodeLibDirectory, "node_modules");
        modules.mkdirs();
        ua.setDestDirectory(modules);
        log.info("Unzipping npm to " + modules.getAbsolutePath());
        try {
            ua.extract();
        } catch (ArchiverException e) {
            log.error("Cannot unzip NPM", e);
            throw new IOException(e);
        }

        // Rename 'package' to 'npm'
        File out = new File(modules, "package");
        if (out.isDirectory()) {
            out.renameTo(new File(modules, "npm"));
        } else {
            throw new IOException("Failed to install NPM - cannot find the package directory");
        }
    }

    private void downloadAndInstallNode() throws IOException {
        URL url;
        String path;
        String version = Constants.NODE_VERSION;
        if (ExecUtils.isWindows()) {
            if (ExecUtils.is64bits()) {
                url = new URL(getNodeDist() + Constants.NODE_VERSION + "/x64/node.exe");
            } else {
                url = new URL(getNodeDist() + Constants.NODE_VERSION + "/node.exe");
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

            downloadAndInstallNPM();
            return;
        } else if (ExecUtils.isMac()) {
            if (!ExecUtils.is64bits()) {
                path = "node-v" + Constants.NODE_VERSION + "-darwin-x86";
                url = new URL(getNodeDist() + Constants.NODE_VERSION + "/node-v" + Constants.NODE_VERSION + "-darwin-x86" +
                        ".tar.gz");
            } else {
                path = "node-v" + Constants.NODE_VERSION + "-darwin-x64";
                url = new URL(getNodeDist() + Constants.NODE_VERSION + "/node-v" + Constants.NODE_VERSION + "-darwin-x64" +
                        ".tar.gz");
            }
        } else if (ExecUtils.isLinux()) {
            if (ExecUtils.isARM()) {
                // ARM needs to use a specific version of node. This version may not be the same as the x86 or x64
                // version.
                version = Constants.NODE_VERSION_ARM;
                path = "node-v" + Constants.NODE_VERSION_ARM + "-linux-arm-pi";
                url = new URL(getNodeDist() + Constants.NODE_VERSION_ARM + "/node-v"
                        + Constants.NODE_VERSION_ARM + "-linux-arm-pi.tar.gz");
            } else if (ExecUtils.is64bits()) {
                path = "node-v" + Constants.NODE_VERSION + "-linux-x64";
                url = new URL(getNodeDist() + Constants.NODE_VERSION + "/node-v"
                        + Constants.NODE_VERSION + "-linux-x64.tar.gz");
            } else {
                path = "node-v" + Constants.NODE_VERSION + "-linux-x86";
                url = new URL(getNodeDist() + Constants.NODE_VERSION + "/node-v"
                        + Constants.NODE_VERSION + "-linux-x86.tar.gz");
            }
        } else {
            throw new UnsupportedOperationException("Operating system `" + System.getProperty("os.name") + "` not " +
                    "supported");
        }

        File tmp = File.createTempFile("nodejs", ".tar.gz");
        log.info("Downloading nodejs-" + version + " from " + url.toExternalForm());
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

//        // Delete the installed npm if any
//        if (npmDirectory.isDirectory()) {
//            FileUtils.deleteDirectory(npmDirectory);
//        }

    }

    public File getWorkDir() {
        return mojo.basedir;
    }
}
