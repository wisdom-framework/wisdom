package org.ow2.chameleon.wisdom.cache.ehcache;

import org.junit.Before;
import org.junit.Test;
import org.ow2.chameleon.wisdom.api.cache.Cache;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.test.parents.Action;
import org.ow2.chameleon.wisdom.test.parents.Invocation;
import org.ow2.chameleon.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ow2.chameleon.wisdom.test.parents.Action.action;

/**
 * Test the Cache service.
 */
public class EhCacheServiceIT extends WisdomTest {

    @Inject
    Cache service;

    @Inject
    MyCachedController controller;

    @Test
    public void testInsertionAndExpiration() throws InterruptedException {
        service.set("key", "value", 1);
        assertThat(service.get("key")).isEqualTo("value");
        Thread.sleep(1000);
        assertThat(service.get("key")).isNull();
    }

    @Test
    public void testCacheFromController() throws InterruptedException {
        Action.ActionResult result = action(new Invocation() {

            @Override
            public Result invoke() throws Throwable {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("100");

        result = action(new Invocation() {

            @Override
            public Result invoke() throws Throwable {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("101");

        Thread.sleep(2000);

        result = action(new Invocation() {

            @Override
            public Result invoke() throws Throwable {
                return controller.retrieve();
            }
        }).invoke();

        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).isEqualTo("100");
    }
}
