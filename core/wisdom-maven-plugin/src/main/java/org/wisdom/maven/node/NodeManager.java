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

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.InstallationException;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;
import org.apache.maven.plugin.logging.Log;
import org.wisdom.maven.Constants;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.utils.ExecUtils;
import org.wisdom.maven.utils.MojoUtils;

import java.io.File;
import java.io.IOException;

/**
 * A class managing node and npm
 * Must be sure that .npmrc file does not set the prefix.
 */
public class NodeManager {

    private final AbstractWisdomMojo mojo;
    private final FrontendPluginFactory factory;
    private final File nodeDirectory;
    private final FrontendPluginFactory npmInstallationFactory;


    public NodeManager(AbstractWisdomMojo mojo) {
        this(mojo.getLog(), new File(System.getProperty("user.home"), ".wisdom/node/"
                + Constants.NODE_VERSION), mojo);
    }

    public NodeManager(Log log, File nodeDirectory, AbstractWisdomMojo mojo) {
        this.factory = new FrontendPluginFactory(mojo.basedir, nodeDirectory);
        this.npmInstallationFactory = new FrontendPluginFactory(nodeDirectory, nodeDirectory);
        this.nodeDirectory = nodeDirectory;
        this.mojo = mojo;
        if (!nodeDirectory.exists()) {
            nodeDirectory.mkdirs();
        }
    }

    /**
     * @return the factory used for NPM execution.
     */
    public FrontendPluginFactory factory() {
        return factory;
    }

    /**
     * @return the factory used for NPM installation. This factory sets the working directory to the node installation
     * directory in order to install the NPM in a known location (installation occurs locally, not globally).
     */
    public FrontendPluginFactory factoryForNPMInstallation() {
        return npmInstallationFactory;
    }

    /**
     * Installs node in ~/.wisdom/node/$version.
     * The installation process is the following:
     * <ol>
     * <li>download node</li>
     * <li>expand node to the right location</li>
     * </ol>
     * <p/>
     *
     * @throws java.io.IOException
     */
    public void installIfNotInstalled() throws IOException {
        try {
            factory.getNodeAndNPMInstaller(proxy())
                    .install(mojo.getNodeVersion(),
                            mojo.getNPMVersion(),
                            mojo.getNodeDistributionRootUrl(),
                            mojo.getNpmRegistryRootUrl() + "/npm/-/");
            if (!getNodeExecutable().isFile()) {
                throw new IOException("Node installation failed - " + getNodeExecutable().getAbsolutePath() + " does not exist");
            }
        } catch (InstallationException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return the proxy settings for NPM execution.
     */
    public ProxyConfig proxy() {
        return MojoUtils.getProxyConfig(mojo.session, mojo.decrypter);
    }

    /**
     * @return the node executable.
     */
    public File getNodeExecutable() {
        if (ExecUtils.isWindows()) {
            return new File(nodeDirectory, "node/node.exe");
        } else {
            return new File(nodeDirectory, "node/node");
        }
    }

    /**
     * @return the NPM installation directory ({@code node_modules} directory).
     */
    public File getNodeModulesDirectory() {
        return new File(nodeDirectory, "node_modules");
    }

    /**
     * @return the working directory for NPM execution (not installation).
     */
    public File getWorkDir() {
        return mojo.basedir;
    }
}
