package org.ow2.chameleon.wisdom.cache.ehcache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.chameleon.wisdom.api.cache.Cache;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the Cache service.
 */
public class EhCacheServiceTest {

    EhCacheService service;

    @Before
    public void setUp() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBaseDir()).thenReturn(new File(""));
        service = new EhCacheService(configuration);
    }

    @After
    public void tearDown() {
        service.stop();
    }

    @Test
    public void testInsertionAndExpiration() throws InterruptedException {
        service.set("key", "value", 1);
        assertThat(service.get("key")).isEqualTo("value");
        Thread.sleep(1000);
        assertThat(service.get("key")).isNull();
    }
}
