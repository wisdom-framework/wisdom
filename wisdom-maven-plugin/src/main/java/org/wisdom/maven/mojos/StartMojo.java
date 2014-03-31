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
package org.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.utils.WisdomExecutor;

import java.io.File;
import java.io.IOException;

/**
 * Starts a Wisdom server in background. The Wisdom instance is similar to the instance running with the 'run' goals
 * but: runs in background, does not have the 'watch' mode enabled. To stop the running server, use the 'stop' mojo.
 */
@Mojo(name = "start", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true
)
@Execute(phase = LifecyclePhase.PACKAGE)
public class StartMojo extends AbstractWisdomMojo {

    /**
     * Starts the server.
     *
     * @throws MojoExecutionException if the server cannot be started
     */
    @Override
    public void execute() throws MojoExecutionException {
        new WisdomExecutor().executeInBackground(this);
        File pid = new File(getWisdomRootDirectory(), "RUNNING_PID");
        if (WisdomExecutor.waitForFile(pid)) {
            try {
                getLog().info("Wisdom launched in background in process '" + FileUtils.readFileToString(pid) + "'.");
            } catch (IOException e) {
                getLog().warn("Cannot read the 'RUNNING_PID' file", e);
            }
        } else {
            getLog().error("The " + pid.getName() + " file was not created despite the 'successful' launch of Wisdom");
        }
    }


}
