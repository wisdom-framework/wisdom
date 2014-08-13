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
package org.wisdom.maven.pipeline;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipelinesTest {

    @Test
    public void testNoWatchers() throws Exception {
        MavenSession session = mock(MavenSession.class);
        when(session.getExecutionProperties()).thenReturn(new Properties());
        File baseDir = new File("target/junk");
        Mojo mojo = mock(Mojo.class);
        Log log = mock(Log.class);
        when(mojo.getLog()).thenReturn(log);
        Pipelines.watchers(session, baseDir, mojo, true);
    }
}
