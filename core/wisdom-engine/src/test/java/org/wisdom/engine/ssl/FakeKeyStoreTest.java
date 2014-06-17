package org.wisdom.engine.ssl;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FakeKeyStoreTest {

    @Test
    public void testKeyManagerFactory() throws Exception {
        File root = new File("target/tmp/security");
        root.mkdirs();
        File conf = new File(root, "conf");
        conf.mkdirs();
        FakeKeyStore.keyManagerFactory(root);
        assertThat(new File(conf, "fake.keystore")).isFile();
    }
}