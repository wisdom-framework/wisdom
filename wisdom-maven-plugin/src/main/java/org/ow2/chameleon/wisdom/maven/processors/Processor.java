package org.ow2.chameleon.wisdom.maven.processors;

import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.util.Map;

/**
 * Processors are plugged on the pipeline, and call in sequence.
 */
public interface Processor extends Constants {


    public void configure(AbstractWisdomMojo mojo);

    public void processAll() throws ProcessorException;

    public void tearDown();

    public boolean accept(File file);

    public boolean fileCreated(File file) throws ProcessorException;

    public boolean fileUpdated(File file) throws ProcessorException;

    public boolean fileDeleted(File file) throws ProcessorException;

}
