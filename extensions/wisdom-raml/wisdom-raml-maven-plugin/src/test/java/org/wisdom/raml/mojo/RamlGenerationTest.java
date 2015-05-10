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
package org.wisdom.raml.mojo;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * check the RAML generation.
 */
public class RamlGenerationTest {

    private RamlCompilerMojo mojo;

    public static final String RAML_OUTPUT = "raml"+File.separator+"FakeController.raml";

    @Before
    public void setUp() throws IOException {
        //init the mojo

        MockitoAnnotations.initMocks(this);
        mojo = new RamlCompilerMojo();
        mojo.project= mock(MavenProject.class);
        when(mojo.project.getVersion()).thenReturn("1.0-SNAPSHOT");
    }

    @Test
    public void testGenerationOnEventController() throws IOException, WatchingException {
        String input = "EventsController.java";
        File output = new File("target/wisdom/assets/raml/EventsController.raml");
        mojo.parseController(new File("src/test/resources/controllers", input));
        assertThat(output).exists();
        assertThat(FileUtils.readFileToString(output)).containsOnlyOnce("/events: \n" +
                "    get: \n" +
                "    /{id}: \n" +
                "        uriParameters: \n" +
                "            id: \n" +
                "                type: number\n" +
                "                required: true\n" +
                "                repeat: false\n" +
                "        get: ");
    }

    @Test
    public void testGenerationOnNotChildrenController() throws IOException, WatchingException {
        String input = "NotChildrenController.java";
        File output = new File("target/wisdom/assets/raml/NotChildrenController.raml");
        mojo.parseController(new File("src/test/resources/controllers", input));
        assertThat(output).exists();
        assertThat(FileUtils.readFileToString(output)).containsOnlyOnce("/hello/tricky: \n" +
                "    options: \n" +
                "/hello/trickyNotAChild: \n" +
                "    put: \n" +
                "documentation: \n" +
                "    - \n" +
                "        title: Description\n" +
                "        content: A good old fake controller.");
    }
}
