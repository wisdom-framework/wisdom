package org.ow2.chameleon.wisdom.maven.processors;

/**
 * Pipeline bootstrap
 */
public class Pipelines {

    public static  Pipeline watcher() {
        Pipeline pipeline = new Pipeline();
        pipeline.addLast(new CopyConfigurationProcessor());
        pipeline.addLast(new CopyExternalAssetProcessor());
        pipeline.addLast(new CopyExternalTemplateProcessor());
        pipeline.addLast(new CopyInternalResourcesProcessor());
        pipeline.addLast(new SourceCompilerProcessor());
        pipeline.addLast(new BundlePackagerProcessor());
        pipeline.addLast(new BundleDeployerProcessor());

        return pipeline;
    }
}
