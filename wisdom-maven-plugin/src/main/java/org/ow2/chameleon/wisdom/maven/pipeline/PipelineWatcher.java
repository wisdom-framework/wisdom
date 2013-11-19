package org.ow2.chameleon.wisdom.maven.pipeline;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

/**
 *
 */
public class PipelineWatcher extends FileAlterationListenerAdaptor  {

    private final Pipeline pipeline;

    public PipelineWatcher(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void onFileCreate(File file) {
        pipeline.onFileCreate(file);
    }

    @Override
    public void onFileChange(File file) {
        pipeline.onFileChange(file);
    }

    @Override
    public void onFileDelete(File file) {
        pipeline.onFileDelete(file);
    }
}
