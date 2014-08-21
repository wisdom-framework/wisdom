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

    @Parameter(required = true)
    private String name;

    @Parameter(required = false)
    private String version;

    @Parameter(required = true)
    private String binary;

    @Parameter(defaultValue = "", required = true)
    private String[] arguments;

    @Parameter(required = false)
    private String watchFilter;


    public void execute()
            throws MojoExecutionException {
        NPM.npm(this, name, version).execute(binary, arguments);
    }

    /**
     * The markdown mojo only accepts Markdown files, i.e. files using the {@code .md, .markdown} extensions,
     * or onle of the custom extensions set.
     *
     * @param file is the file.
     * @return {@code true} if the file is accepted.
     */
    @Override
    public boolean accept(File file) {
        if (watchFilter == null) {
            return false;
        }
        WildcardFileFilter filter = new WildcardFileFilter(watchFilter);
        return filter.accept(file);
    }

    /**
     * An accepted file was created - processes it.
     *
     * @param file is the file.
     * @return {@code true}
     * @throws org.wisdom.maven.WatchingException if the file cannot be processed correctly
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
     * An accepted file was updated - re-processes it.
     *
     * @param file is the file.
     * @return {@code true}
     * @throws org.wisdom.maven.WatchingException if the file cannot be processed correctly
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * An accepted file was deleted - deletes the output file.
     *
     * @param file the file
     * @return {@code true}
     */
    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        fileCreated(file);
        return true;
    }
}
