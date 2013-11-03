package org.ow2.chameleon.wisdom.maven.processors;

/**
 * Pipeline bootstrap
 */
public class Pipelines {

    public static  Pipeline watcher() {
        return new Pipeline()
            .addLast(new CopyConfigurationProcessor())
            .addLast(new CopyExternalAssetProcessor())
            .addLast(new CopyExternalTemplateProcessor())
            .addLast(new CopyInternalResourcesProcessor())
            .addLast(new SourceCompilerProcessor())
            .addLast(new BundlePackagerProcessor())
            .addLast(new BundleDeployerProcessor());
    }

    public static Pipeline resourceProcessing() {
        return new Pipeline()
                .addLast(new CopyConfigurationProcessor())
                .addLast(new CopyExternalAssetProcessor())
                .addLast(new CopyExternalTemplateProcessor())
                .addLast(new CopyInternalResourcesProcessor());
    }
}
