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
package org.wisdom.mojo.npm;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.wisdom.maven.node.NPM;

import java.io.File;


/**
 * A plugin installing and executing a specified NPM.
 */
@Mojo(name = "npm-run", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true)
public class NpmRunnerMojo extends AbstractWisdomWatcherMojo implements Constants {

    /**
     * The name of the NPM.
     */
    @Parameter(required = true)
    String name;

    /**
     * The version of the NPM. Can be omitted.
     */
    @Parameter(required = false)
    String version;

    /**
     * The name of the executable to launch. If omitted, use the npm name.
     */
    @Parameter(required = true)
    String binary;

    /**
     * The execution arguments.
     */
    @Parameter(required = true)
    String[] arguments;

    /**
     * An optional filter to re-execute the NPM when a file matching the filter is created, updated or deleted.
     */
    @Parameter(required = false)
    String watchFilter;


    /**
     * Installs and executes the NPM.
     *
     * @throws MojoExecutionException if the execution fails.
     */
    public void execute()
            throws MojoExecutionException {
        if (watchFilter == null) {
            removeFromWatching();
        }
        NPM.npm(this, name, version).execute(
                binary != null ? binary : name, arguments);
    }

    /**
     * If the watcher has a filter set, creates a wildcard filter and test the file name against this filter.
     *
     * @param file is the file.
     * @return {@code true} if the file is accepted.
     */
    @Override
    public boolean accept(File file) {
        WildcardFileFilter filter = new WildcardFileFilter(watchFilter);
        return filter.accept(file);
    }

    /**
     * An accepted file was created - executes the NPM.
     *
     * @param file is the file.
     * @return {@code true}
     * @throws org.wisdom.maven.WatchingException if the execution fails.
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            execute();
        } catch (MojoExecutionException e) {
            throw new WatchingException("Cannot execute the NPM '" + name + "'", e);
        }
        return true;
    }

    /**
     * An accepted file was updated - executes the NPM.
     *
     * @param file is the file.
     * @return {@code true}
     * @throws org.wisdom.maven.WatchingException if the execution fails.
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * An accepted file was deleted  - executes the NPM.
     *
     * @param file the file
     * @return {@code true}
     * @throws org.wisdom.maven.WatchingException if the execution fails.
     */
    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        fileCreated(file);
        return true;
    }
}
