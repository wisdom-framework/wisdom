package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;

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
        return null; // Not found.
    }

    public static Xpp3Dom getBuildPluginMainConfiguration(AbstractWisdomMojo mojo, String plugin) {
        List<Plugin> plugins = mojo.project.getBuildPlugins();
        for (Plugin plug : plugins) {
            if (plug.getArtifactId().equals(plugin)) {
                Xpp3Dom conf = (Xpp3Dom) plug.getConfiguration();
            }
        }
        return null; // Not found.
    }

}
