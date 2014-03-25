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
package org.wisdom.maven.node;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Check the NPM behavior.
 */
public class NPMTest {

    @Test
    public void testExtractVersionFromPackageJson() {
        Log log = mock(Log.class);
        File coffeescript = new File("target/test-classes/package-json/coffeescript");
        String version = NPM.getVersionFromNPM(coffeescript, log);
        assertThat(version).isEqualTo("1.7.1");

        File less = new File("target/test-classes/package-json/less");
        version = NPM.getVersionFromNPM(less, log);
        assertThat(version).isEqualTo("1.5.0");

        File doesNotExist = new File("target/test-classes/package-json/nope");
        version = NPM.getVersionFromNPM(doesNotExist, log);
        assertThat(version).isEqualTo("0.0.0");
    }
}
