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
package org.wisdom.maven.utils;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.util.List;

/**
 * Retrieves information about plugins of the built project.
 */
public class PluginExtractor {

    public static String getBuildPluginVersion(AbstractWisdomMojo mojo, String plugin) {
        List<Plugin> plugins = mojo.project.getBuildPlugins();
        for (Plugin plug : plugins) {
            if (plug.getArtifactId().equals(plugin)) {
                return plug.getVersion();
            }
        }
        // Not found.
        return null;
    }

    public static Xpp3Dom getBuildPluginMainConfiguration(AbstractWisdomMojo mojo, String plugin) {
        List<Plugin> plugins = mojo.project.getBuildPlugins();
        for (Plugin plug : plugins) {
            if (plug.getArtifactId().equals(plugin)) {
                return  (Xpp3Dom) plug.getConfiguration();
            }
        }
        // Not found.
        return null;
    }

    public static Xpp3Dom getBuildPluginConfigurationForGoal(AbstractWisdomMojo mojo, String plugin, String goal) {
        List<Plugin> plugins = mojo.project.getBuildPlugins();
        for (Plugin plug : plugins) {
            if (plug.getArtifactId().equals(plugin)) {
                // Check main execution
                List<String> globalGoals = (List<String>) plug.getGoals();
                if (globalGoals != null  && globalGoals.contains(goal)) {
                    return  (Xpp3Dom) plug.getConfiguration();
                }
                // Check executions
                for (PluginExecution execution : plug.getExecutions()) {
                    if (execution.getGoals().contains(goal)) {
                        return  (Xpp3Dom) execution.getConfiguration();
                    }
                }
            }
        }
        // Not found.
        return null;
    }

}
