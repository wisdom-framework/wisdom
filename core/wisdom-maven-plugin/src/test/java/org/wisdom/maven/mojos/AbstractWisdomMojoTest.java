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