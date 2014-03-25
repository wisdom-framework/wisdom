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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.utils.WisdomExecutor;

import java.io.File;

/**
 * Stop Mojo
 */
@Mojo(name = "stop", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true
)
public class StopMojo extends AbstractWisdomMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        new WisdomExecutor().stop(this);
        File pid = new File(getWisdomRootDirectory(), "RUNNING_PID");
        if (WisdomExecutor.waitForFileDeletion(pid)) {
            getLog().info("Wisdom server stopped.");
        } else {
            throw new MojoExecutionException("The " + pid.getName() + " file still exists after having stopped the " +
                    "Wisdom instance - check log");
        }
    }

}
