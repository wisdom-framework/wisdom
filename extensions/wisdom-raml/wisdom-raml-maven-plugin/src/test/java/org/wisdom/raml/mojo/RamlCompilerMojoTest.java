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
/*
 * Copyright 2015, Technologic Arts Vietnam.
 * All right reserved.
 */

package org.wisdom.raml.mojo;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link org.wisdom.raml.mojo.RamlCompilerMojo}
 *
 * @author <a href="mailto:jbardin@tech-arts.com">Jonathan M. Bardin</a>
 */
public class RamlCompilerMojoTest {

    public static final String FAKE_PROJECT = "target/test-classes/fake-project";
    public static final String RAML_OUTPUT = "raml"+File.separator+"FakeController.raml";

    private RamlCompilerMojo mojo;

    private File raml;

    @Before
    public void setUp() throws IOException {
        //init the mojo

        MockitoAnnotations.initMocks(this);
        mojo = new RamlCompilerMojo();
        mojo.basedir = new File(FAKE_PROJECT);
        mojo.project= mock(MavenProject.class);
        when(mojo.project.getVersion()).thenReturn("1.0-SNAPSHOT");
        raml = new File(WatcherUtils.getExternalAssetsDestination(mojo.basedir), RAML_OUTPUT);
    }

    @After
    public void tearDown(){
        FileUtils.deleteQuietly(mojo.buildDirectory);
    }

    @Test
    public void ramlOutPutShouldExistForFakeController() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.execute();
        assertThat(raml).exists();
    }

    @Test
    public void ramlShouldContainsGoodRoute() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.execute();

        List<String> lines = FileUtils.readLines(raml);
        assertThat(lines).containsOnlyOnce(
                "/hello: ",
                "    /carembar/{pepper}: ",
                "    /login: ",
                "    /tricky: ",
                "    /trickyNotAChild: ",
                "    /{name}: ",
                "        /french: ",
                "        /spanish: ",
                "            /filter: ");
    }

    @Test
    public void ramlShouldContainsBodyType() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.execute();

        List<String> lines = FileUtils.readLines(raml);
        assertThat(lines).containsOnlyOnce(
                "                    text/plain: ",
                "                text/json: ",
                "                text/xml: ",
                "                        text/json: ");
    }
    @Test
    public void ramlShouldContainsDocumentationFromClassJDoc() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.execute();

        List<String> lines = FileUtils.readLines(raml);
        assertThat(lines).containsOnlyOnce(
                "documentation: ",
                "        content: A good old fake controller.");
    }

    @Test
    public void ramlShouldContainFormParam() throws  MojoFailureException,MojoExecutionException,IOException{
        mojo.execute();

        List<String> lines = FileUtils.readLines(raml);
        assertThat(lines).containsOnlyOnce(
            "                application/x-www-form-urlencoded: ",
            "                    formParameters: ",
            "                        email: ",
            "                        pass: ");
    }

    @Test
    public void ramlOutPutForFakeControllerNoJDocShouldExist() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.execute();
        assertThat(new File(WatcherUtils.getExternalAssetsDestination(mojo.basedir), "raml"+File.separator+"FakeControllerNoJDoc.raml")).exists();
    }
}
