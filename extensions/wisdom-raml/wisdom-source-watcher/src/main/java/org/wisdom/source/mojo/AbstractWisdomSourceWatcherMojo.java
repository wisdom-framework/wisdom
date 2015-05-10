/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.source.mojo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.wisdom.maven.utils.WatcherUtils;
import org.wisdom.source.ast.model.ControllerModel;
import org.wisdom.source.ast.visitor.ClassSourceVisitor;
import org.wisdom.source.ast.visitor.ControllerSourceVisitor;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * Abstract Mojo that extends {@link AbstractWisdomWatcherMojo}. It watch the wisdom source file and create a Java model
 * from them thanks to the {@link JavaParser}.
 *
 * It call the method {@link #controllerParsed(File, ControllerModel)} each time a controller
 * has been modified and its model made.
 * </p>
 *
 * @param <T> The type of the object that will be passed to the visitor along the visit of the ControllerModel.
 */
public abstract class AbstractWisdomSourceWatcherMojo<T> extends AbstractWisdomWatcherMojo implements Constants{

    /**
     * Location of the project Java sources.
     */
    protected File javaSourceDir;

    /**
     * Visit the Controller java source in order to create the Controller model.
     */
    private static final ControllerSourceVisitor CONTROLLER_VISITOR = new ControllerSourceVisitor();

    private static final ClassSourceVisitor CLASS_VISITOR = new ClassSourceVisitor();

    /**
     * Create a model for each wisdom controller available in the java source directory and call
     * {@link #controllerParsed(File, ControllerModel)} for each of them.
     *
     * @throws MojoExecutionException when an exception occurred while creating the raml file.
     * @throws MojoFailureException  {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        javaSourceDir = WatcherUtils.getJavaSource(basedir);

        try {
            for (File file : FileUtils.listFiles(javaSourceDir, new String[]{"java"}, true)) {
                if (accept(file)) {
                    parseController(file);
                }
            }
        }catch (Exception we){
            throw new MojoExecutionException("An exception occurred while created raml file",we);
        }
    }

    /**
     * Parse the source file of a wisdom Controller and create a model from it.
     * Call {@link #controllerParsed(File, ControllerModel)}.
     *
     * @param file the controller source file.
     * @throws WatchingException
     */
    public void parseController(File file) throws WatchingException{
        ControllerModel<T> controllerModel = new ControllerModel<>();

        //Populate the controller model by visiting the File
        try {
            JavaParser.parse(file).accept(CONTROLLER_VISITOR,controllerModel);
        } catch (ParseException |IOException e) {
            throw new WatchingException("Cannot parse "+file.getName(), e);
        }

        //Call children once the controller has been parsed
        controllerParsed(file, controllerModel);
    }

    /**
     * Check if we can create a model from the given source.
     *
     * @param file {@link File} required to be processed by this plugin.
     * @return <code>true</code> if the <code>file</code> implements
     *  {@link org.wisdom.api.Controller}, <code>false</code> otherwise.
     */
    public boolean accept(File file) {
        if( !WatcherUtils.isInDirectory(file, javaSourceDir) || !WatcherUtils.hasExtension(file,"java")){
            return false;
        }

        // If the file has been deleted by may have been a controller, return true.
        // The cleanup will be applied.
        if (! file.isFile()) {
            return true;
        }

        //Parse the Java File and check if it's a wisdom Controller
        try {
            final CompilationUnit parse = JavaParser.parse(file);
            // The visitor return a Boolean object, potentially null.
            final Boolean accept = parse.accept(CLASS_VISITOR, null);
            return accept != null  && accept;
        } catch (Exception e) {
            getLog().error("Cannot parse  " + file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Create a model for the new controller and call {@link #controllerParsed(File, ControllerModel)}.
     *
     * @param file The source file that has created.
     * @return {@inheritDoc}
     * @throws WatchingException {@inheritDoc}
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        parseController(file);
        return true;
    }

    /**
     * Create a new model for the controller that has been modified and
     * call {@link #controllerParsed(File, ControllerModel)}.
     *
     * @param file the controller that has been updated.
     * @return {@inheritDoc}
     * @throws WatchingException {@inheritDoc}
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        parseController(file);
        return true;
    }

    /**
     * Is called when the controller source has been properly visited and its model created.
     *
     * @param source The file source
     * @param model The model of the source
     * @throws WatchingException
     */
    public abstract void controllerParsed(File source, ControllerModel<T> model) throws WatchingException;
}
