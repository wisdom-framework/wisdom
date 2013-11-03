package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.ow2.chameleon.wisdom.maven.processors.Pipelines;
import org.ow2.chameleon.wisdom.maven.processors.ProcessorException;

/**
 * Mojo copying and processing project resources.
 */
@Mojo(name = "process-resources", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ResourceProcessingMojo extends AbstractWisdomMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Pipelines.resourceProcessing().initialize(this);
        } catch (ProcessorException e) {
            throw new MojoExecutionException("An error happened while copying and processing resources", e);
        }
    }

}
