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

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractWisdomMojoTest {

    @Test
    public void testGetWisdomRootDirectoryWhenWisdomDirectoryIsNotSet() throws Exception {
        DummyMojo mojo = new DummyMojo();
        mojo.basedir = new File("target/dummy");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        File expected = new File(mojo.basedir, "target/wisdom");
        assertThat(mojo.getWisdomRootDirectory()).isEqualTo(expected);
    }

    @Test
    public void testGetWisdomRootDirectoryWhenWisdomDirectoryIsSet() throws Exception {
        DummyMojo mojo = new DummyMojo();
        mojo.basedir = new File("target/dummy");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        mojo.wisdomDirectory = new File(mojo.basedir, "target/wisdom2");
        assertThat(mojo.getWisdomRootDirectory()).isEqualTo(mojo.wisdomDirectory);
    }

    private class DummyMojo extends AbstractWisdomMojo {

        @Override
        public void execute() {
            // Does nothing on purpose.
        }
    }
}