package org.wisdom.ebean.mojo;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the Ebean Transformer Mojo
 */
public class EbeanTransformerMojoTest {
    @Test
    public void testExtractPath() throws Exception {
        File file = new File("target/classes/org/wisdom/ebean/mojo/EbeanTransformerMojo.class");
        assertThat(EbeanTransformerMojo.extractPathFromAgentMessage("Enhanced " + file)).isEqualTo(file.getPath());
    }

    @Test
    public void testClassNameComputationFromPath() throws Exception {
        File file = new File("target/classes/org/wisdom/ebean/mojo/EbeanTransformerMojo.class");
        File sources = new File("target/classes");
        assertThat(EbeanTransformerMojo.getClassName(file.getPath(), sources.getPath())).isEqualTo
                (EbeanTransformerMojo.class.getName());
    }

    @Test
    public void testHeaderComputationWithOneClass() throws Exception {
        assertThat(EbeanTransformerMojo.computeHeader(ImmutableList.of(EbeanTransformerMojo.class.getName())))
                .isEqualTo(EbeanTransformerMojo.class.getName());
    }

    @Test
    public void testHeaderComputationWithTwoClasses() throws Exception {
        assertThat(EbeanTransformerMojo.computeHeader(ImmutableList.of(
                EbeanTransformerMojo.class.getName(),
                List.class.getName())))
                .isEqualTo(EbeanTransformerMojo.class.getName() + ", " + List.class.getName());
    }
}
