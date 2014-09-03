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
package org.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.CompilerExecutor;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.Collection;

/**
 * Compiles tests sources.
 */
@Mojo(name = "testCompile", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true,
        defaultPhase = LifecyclePhase.TEST_COMPILE)
public class JavaTestCompilerMojo extends AbstractWisdomWatcherMojo implements Constants {
    private File classes;
    private CompilerExecutor compiler = new CompilerExecutor();

    /**
     * Compiles the Java sources.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException thrown on compilation error.
     */
    @Override
    public void execute() throws MojoExecutionException {
        classes = new File(buildDirectory, "test-classes");
        compiler.executeForTests(this);
    }

    /**
     * Checks whether or not an event on the given file should trigger the Java compilation.
     *
     * @param file the file
     * @return {@literal true} if the given file is a Java file and is contained in the Java test source directory.
     */
    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, new File(basedir, "src/test/java"));
    }

    /**
     * A new (accepted) file was created. This methods triggers the Java compilation.
     *
     * @param file the file
     * @return {@literal true}
     * @throws org.wisdom.maven.WatchingException thrown on compilation error. The thrown exception contains the file, line,
     *                           character and reason of the compilation error.
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        compile();
        return true;
    }

    private void compile() throws WatchingException {
        try {
            execute();
        } catch (MojoExecutionException e) {
            if (e.getCause() != null
                    && e.getCause().getClass().getName().equals("org.apache.maven.plugin.compiler" +
                    ".CompilationFailureException")) {
                throw CompilerExecutor.build(this, e.getCause());
            }
            throw new WatchingException("Compilation error", e);
        }
    }

    /**
     * A new (accepted) file was updated. This methods triggers the Java compilation.
     *
     * @param file the file
     * @return {@literal true}
     * @throws org.wisdom.maven.WatchingException thrown on compilation error. The thrown exception contains the file, line,
     *                           character and reason of the compilation error.
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        compile();
        return true;
    }

    /**
     * A new (accepted) file was deleted. This methods triggers the Java compilation.
     *
     * @param file the file
     * @return {@literal true}
     * @throws org.wisdom.maven.WatchingException thrown on compilation error. The thrown exception contains the file, line,
     *                           character and reason of the compilation error.
     */
    @Override
    public boolean fileDeleted(final File file) throws WatchingException {
        // Delete the associated class file.
        // We delete more than required... but the inner class case is very tricky.
        Collection<File> files = FileUtils.listFiles(classes, new IOFileFilter() {
            @Override
            public boolean accept(File test) {
                String classname = FilenameUtils.getBaseName(test.getName());
                String filename = FilenameUtils.getBaseName(file.getName());
                return classname.equals(filename) || classname.startsWith(filename + "$");
            }

            @Override
            public boolean accept(File dir, String name) {
                return accept(new File(dir, name));
            }
        }, TrueFileFilter.INSTANCE);

        for (File clazz : files) {
            getLog().debug("Deleting " + clazz.getAbsolutePath() + " : " + clazz.delete());
        }

        compile();
        return true;
    }
}
