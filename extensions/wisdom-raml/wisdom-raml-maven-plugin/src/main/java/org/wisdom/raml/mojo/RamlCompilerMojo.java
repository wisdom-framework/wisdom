/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2014 - 2015 Wisdom Framework
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
package org.wisdom.raml.mojo;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.raml.emitter.RamlEmitter;
import org.raml.model.Raml;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.WatcherUtils;
import org.wisdom.raml.visitor.RamlControllerVisitor;
import org.wisdom.source.ast.model.ControllerModel;
import org.wisdom.source.mojo.AbstractWisdomSourceWatcherMojo;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * This wisdom plugin generate a raml file for each wisdom Controller, thanks to the wisdom annotations
 * and configuration.
 * </p>
 *
 * @author barjo
 */
@Mojo(name = "create-raml", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class RamlCompilerMojo extends AbstractWisdomSourceWatcherMojo<Raml> implements Constants {

    /**
     * Visit the controller model in order to create the raml spec file.
     */
    private final RamlControllerVisitor controllerVisitor = new RamlControllerVisitor();

    /**
     * Use in order to dump the Raml spec object as a file.
     */
    private final RamlEmitter ramlEmitter = new RamlEmitter();

    /**
     * The root uri of each routes.
     */
    @Parameter(defaultValue = "http://localhost:9000")
    private String baseUri;

    /**
     * The directory in which raml file are created. By default it's `target/wisdom/assets/raml`.
     * You can change it to `target/classes/assets/raml` to embed your raml files inside your bundle.
     * <p>
     * Be aware that the runtime support from Wisdom is looking for "/assets/raml/*.raml" files.
     */
    @Parameter
    private String outputDirectory;

    /**
     * Generate the raml file from a given controller source file.
     *
     * @param source The controller source file.
     * @param model  The controller model
     * @throws WatchingException If there is a problem while creating the raml file.
     */
    @Override
    public void controllerParsed(File source, ControllerModel<Raml> model) throws WatchingException {
        Raml raml = new Raml(); //Create a new raml file
        raml.setBaseUri(baseUri); //set the base uri
        raml.setVersion(project().getVersion());

        //Visit the controller model to populate the raml model
        model.accept(controllerVisitor, raml);

        getLog().info("Create raml file for controller " + raml.getTitle());

        try {
            //create the file
            File output = getRamlOutputFile(source);
            FileUtils.write(output, ramlEmitter.dump(raml));
            getLog().info("Created the RAML description for " + source.getName() + " => " + output.getAbsolutePath());
        } catch (IOException ie) {
            throw new WatchingException("Cannot create raml file", source, ie);
        } catch (IllegalArgumentException e) {
            throw new WatchingException("Cannot create Controller Element from", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        File theFile = getRamlOutputFile(file);
        FileUtils.deleteQuietly(theFile);
        return true;
    }

    /**
     * Create the .raml file from the java source file.
     *
     * @param input The java source file.
     * @return The File where the raml spec, for the given input, will be written.
     */
    private File getRamlOutputFile(File input) {
        String ramlFileName = input.getName().substring(0, input.getName().length() - 4) + "raml";

        File outDir;
        if (outputDirectory == null) {
            outDir = new File(WatcherUtils.getExternalAssetsDestination(basedir), "raml");
        } else {
            outDir = new File(basedir, outputDirectory);
        }

        return new File(outDir, ramlFileName);
    }
}
