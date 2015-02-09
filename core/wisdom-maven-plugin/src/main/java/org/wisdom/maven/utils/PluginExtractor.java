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
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.util.List;

/**
 * Retrieves information about plugins of the built project.
 */
public class PluginExtractor {

    /**
     * Retrieves the plugin version from Maven Project.
     *
     * @param mojo   the mojo
     * @param plugin the artifact id of the plugin
     * @return the version, {@code null} if the Maven Project does not used the given plugin
     */
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

    /**
     * Retrieves the main configuration of the given plugin from the Maven Project.
     *
     * @param mojo       the mojo
     * @param artifactId the artifact id of the plugin
     * @param goal       an optional goal. If set if first check for a specific configuration executing this
     *                   goal, if not found, it returns the global configuration
     * @return the configuration, {@code null} if not found
     */
    public static Xpp3Dom getBuildPluginConfiguration(AbstractWisdomMojo mojo, String artifactId, String goal) {
        List<Plugin> plugins = mojo.project.getBuildPlugins();

        Plugin plugin = null;
        for (Plugin plug : plugins) {
            if (plug.getArtifactId().equals(artifactId)) {
                plugin = plug;
            }
        }

        if (plugin == null) {
            // Not found
            return null;
        }

        if (goal != null) {
            // Check main execution
            List<String> globalGoals = (List<String>) plugin.getGoals();
            if (globalGoals != null && globalGoals.contains(goal)) {
                return (Xpp3Dom) plugin.getConfiguration();
            }
            // Check executions
            for (PluginExecution execution : plugin.getExecutions()) {
                if (execution.getGoals().contains(goal)) {
                    return (Xpp3Dom) execution.getConfiguration();
                }
            }
        }
        // Global configuration.
        return (Xpp3Dom) plugin.getConfiguration();
    }

    /**
     * Retrieves the configuration for a specific goal of the given plugin from the Maven Project.
     *
     * @param mojo   the mojo
     * @param plugin the artifact id of the plugin
     * @param goal   the goal
     * @return the configuration, {@code null} if not found
     */
    public static Xpp3Dom getBuildPluginConfigurationForGoal(AbstractWisdomMojo mojo, String plugin, String goal) {
        List<Plugin> plugins = mojo.project.getBuildPlugins();
        for (Plugin plug : plugins) {
            if (plug.getArtifactId().equals(plugin)) {
                // Check main execution
                List<String> globalGoals = (List<String>) plug.getGoals();
                if (globalGoals != null && globalGoals.contains(goal)) {
                    return (Xpp3Dom) plug.getConfiguration();
                }
                // Check executions
                for (PluginExecution execution : plug.getExecutions()) {
                    if (execution.getGoals().contains(goal)) {
                        return (Xpp3Dom) execution.getConfiguration();
                    }
                }
            }
        }
        // Not found.
        return null;
    }

    /**
     * Extracts the subset of the given configuration containing only the values accepted by the plugin/goal. The
     * configuration is modified in-place. The the extraction fail the configuration stays unchanged.
     *
     * @param mojo          the Wisdom mojo
     * @param plugin        the plugin object
     * @param goal          the goal / mojo
     * @param configuration the global configuration
     */
    public static void extractEligibleConfigurationForGoal(AbstractWisdomMojo mojo,
                                                           Plugin plugin, String goal, Xpp3Dom configuration) {
        try {
            MojoDescriptor descriptor = mojo.pluginManager.getMojoDescriptor(plugin, goal,
                    mojo.remoteRepos, mojo.repoSession);
            final List<Parameter> parameters = descriptor.getParameters();
            Xpp3Dom[] children = configuration.getChildren();
            if (children != null) {
                for (int i = children.length - 1; i >= 0; i--) {
                    Xpp3Dom child = children[i];
                    if (!contains(parameters, child.getName())) {
                        configuration.removeChild(i);
                    }
                }
            }
        } catch (Exception e) {
            mojo.getLog().warn("Cannot extract the eligible configuration for goal " + goal + " from the " +
                    "configuration");
            mojo.getLog().debug(e);
            // The configuration is not changed.
        }

    }

    private static boolean contains(List<Parameter> parameters, String name) {
        for (Parameter parameter : parameters) {
            if (parameter.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
