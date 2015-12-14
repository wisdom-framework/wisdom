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
package org.wisdom.cache.ehcache;

import org.joda.time.Duration;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.configuration.ApplicationConfiguration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the creation of the EhCache-based cache service implementation.
 */
public class EhCacheServiceTest {

    @Test
    public void test() throws InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("ehcache.enabled", true)).thenReturn(true);
        EhCacheService svc = new EhCacheService();
        svc.configuration = configuration;
        svc.context = mock(BundleContext.class);
        svc.start();

        assertThat(svc.get("key")).isNull();
        svc.set("key", "value", Duration.standardSeconds(1));
        assertThat(svc.get("key")).isEqualTo("value");

        waitForCleanup(svc);
        assertThat(svc.get("key")).isNull();

        svc.set("key", "value", 0);
        assertThat(svc.get("key")).isEqualTo("value");
        assertThat(svc.remove("key")).isTrue();
        assertThat(svc.get("key")).isNull();
        assertThat(svc.remove("missing")).isFalse();

        svc.stop();
    }

    @Test
    public void testDisabled() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("ehcache.enabled", true)).thenReturn(false);
        EhCacheService svc = new EhCacheService();
        svc.configuration = configuration;
        svc.context = mock(BundleContext.class);
        svc.start();

        svc.stop();
    }

    @Test
    public void testUsingGenerics() throws InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("ehcache.enabled", true)).thenReturn(true);
        EhCacheService impl = new EhCacheService();
        impl.configuration = configuration;
        impl.context = mock(BundleContext.class);
        impl.start();

        Cache svc = impl;

        assertThat((String) svc.get("key")).isNull();
        svc.set("key", "value", Duration.standardSeconds(1));
        assertThat(svc.<String>get("key")).contains("value").isEqualTo("value");

        waitForCleanup(impl);
        assertThat(svc.<String>get("key")).isNull();

        svc.set("key", "value", 0);
        final String v = svc.get("key");
        assertThat(v).isEqualTo("value");
        assertThat(svc.remove("key")).isTrue();
        assertThat(svc.<String>get("key")).isNull();
        assertThat(svc.remove("missing")).isFalse();

        impl.stop();
    }

    @Test
    public void testUsingGenericsAndObjects() throws InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("ehcache.enabled", true)).thenReturn(true);
        EhCacheService impl = new EhCacheService();
        impl.configuration = configuration;
        impl.context = mock(BundleContext.class);
        impl.start();

        Cache svc = impl;

        assertThat((String) svc.get("key")).isNull();
        svc.set("key", new User("foo"), Duration.standardSeconds(1));
        assertThat(svc.<User>get("key").name).isEqualTo("foo");

        waitForCleanup(impl);
        assertThat(svc.<String>get("key")).isNull();

        impl.set("key", new User("wisdom"), 0);
        assertThat(svc.<User>get("key")).isEqualTo(new User("wisdom"));
        assertThat(svc.remove("key")).isTrue();
        assertThat(svc.<User>get("key")).isNull();
        assertThat(svc.remove("missing")).isFalse();

        impl.stop();
    }

    private static class User {
        String name;

        public User(String n) {
            this.name = n;
        }

        public boolean equals(Object u) {
            return u instanceof User  && ((User) u).name.equals(name);
        }
    }

    /**
     * Test #297.
     */
    @Test
    public void testRestart() throws InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("ehcache.enabled", true)).thenReturn(true);
        EhCacheService svc = new EhCacheService();
        svc.configuration = configuration;
        svc.context = mock(BundleContext.class);
        svc.start();

        assertThat(svc.get("key")).isNull();
        svc.set("key", "value", Duration.standardSeconds(1));
        assertThat(svc.get("key")).isEqualTo("value");

        waitForCleanup(svc);
        assertThat(svc.get("key")).isNull();

        svc.set("key", "value", 0);
        assertThat(svc.get("key")).isEqualTo("value");
        assertThat(svc.remove("key")).isTrue();
        assertThat(svc.get("key")).isNull();
        assertThat(svc.remove("missing")).isFalse();

        svc.stop();

        svc.start();

        assertThat(svc.get("key")).isNull();
        svc.set("key", "value", Duration.standardSeconds(1));
        assertThat(svc.get("key")).isEqualTo("value");

        waitForCleanup(svc);
        assertThat(svc.get("key")).isNull();

        svc.set("key", "value", 0);
        assertThat(svc.get("key")).isEqualTo("value");
        assertThat(svc.remove("key")).isTrue();
        assertThat(svc.get("key")).isNull();
        assertThat(svc.remove("missing")).isFalse();

    }

    @Test
    @Ignore("Does not reproduce the race condition")
    public void testPeak() throws InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("ehcache.enabled", true)).thenReturn(true);
        final EhCacheService svc = new EhCacheService();
        svc.configuration = configuration;
        svc.context = mock(BundleContext.class);
        svc.start();

        assertThat(svc.get("key")).isNull();
        svc.set("key", "value", Duration.standardSeconds(60));
        assertThat(svc.get("key")).isEqualTo("value");

        CountDownLatch startSignal = new CountDownLatch(1);
        final int client = 100;
        final CountDownLatch doneSignal = new CountDownLatch(client);
        ExecutorService executor = Executors.newFixedThreadPool(client);
        final AtomicInteger counter = new AtomicInteger();

        for (int i = 1; i < client + 1; ++i) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Object val = svc.get("key");
                    if (val == null || !(val instanceof String) || ((String) val).length() == 0) {
                        counter.incrementAndGet();
                    }
                    doneSignal.countDown();
                }
            });
        }

        startSignal.countDown();
        doneSignal.await(60, TimeUnit.SECONDS);

        assertThat(counter.get()).isEqualTo(0);

        svc.remove("key");

        svc.stop();
    }

    private void waitForCleanup(EhCacheService svc) throws InterruptedException {
        for (int count = 0; count < 5; count++) {
            Object obj = svc.get("key");
            if (obj == null) {
                return;
            } else {
                Thread.sleep(1000);
            }
        }
    }
}
