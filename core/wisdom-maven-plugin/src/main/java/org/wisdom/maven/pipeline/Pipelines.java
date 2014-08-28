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
package org.wisdom.maven.pipeline;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.codehaus.plexus.context.Context;

import java.io.File;

/**
 * Pipeline bootstrap.
 */
public class Pipelines {

    /**
     * Creates a 'watching' pipeline.
     * @param session the maven session
     * @param baseDir the project's base directory
     * @param mojo the 'run' mojo
     * @param pomFileMonitoring flag enabling or disabling the pom file monitoring
     * @return the created pipeline
     */
    public static Pipeline watchers(MavenSession session, File baseDir, Mojo mojo, boolean pomFileMonitoring) {
        return new Pipeline(mojo, baseDir, Watchers.all(session), pomFileMonitoring);
    }

    /**
     * Creates a 'watching' pipeline.
     * @param context the Plexus context
     * @param baseDir the project's base directory
     * @param mojo the 'run' mojo
     * @param pomFileMonitoring flag enabling or disabling the pom file monitoring
     * @return the created pipeline
     */
    public static Pipeline watchers(Context context, File baseDir, Mojo mojo, boolean pomFileMonitoring) {
        return new Pipeline(mojo, baseDir, Watchers.all(context), pomFileMonitoring);
    }
}
