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
import org.wisdom.api.cache.Cache;
import org.wisdom.api.cache.Cached;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.RequestContext;

import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Checks the cached interceptor.
 */
public class CachedActionInterceptorTest {

    @Test
    public void testCaching() throws Exception {
        CachedActionInterceptor interceptor = new CachedActionInterceptor();
        interceptor.cache = mock(Cache.class);
        Cached cached = mock(Cached.class);
        when(cached.duration()).thenReturn(10);
        when(cached.key()).thenReturn("key");

        RequestContext context = mock(RequestContext.class);
        when(context.request()).thenReturn(mock(Request.class));
        Context ctx = mock(Context.class);
        when(context.context()).thenReturn(ctx);
        when(context.context().header(anyString())).thenReturn(null);
        final Result r = Results.ok("Result");
        when(context.proceed()).thenReturn(r);

        Result result = interceptor.call(cached, context);
        assertThat(result.getRenderable().<String>content()).isEqualTo("Result");
        assertThat(result).isEqualTo(r);
        // Check that the result was put in cache.
        verify(interceptor.cache, times(1)).get("key");
        verify(interceptor.cache, times(1)).set("key", r, Duration.standardSeconds(10));

        when(interceptor.cache.get("key")).thenReturn(r);
        result = interceptor.call(cached, context);
        assertThat(result).isEqualTo(r);

        verify(interceptor.cache, times(2)).get("key");
    }

    @Test
    public void testCachingWithoutKey() throws Exception {
        CachedActionInterceptor interceptor = new CachedActionInterceptor();
        interceptor.cache = mock(Cache.class);
        Cached cached = mock(Cached.class);
        when(cached.duration()).thenReturn(10);
        when(cached.key()).thenReturn("");

        RequestContext context = mock(RequestContext.class);
        final Request request = mock(Request.class);
        when(request.uri()).thenReturn("/my/url?withquery");
        when(context.request()).thenReturn(request);
        Context ctx = mock(Context.class);
        when(context.context()).thenReturn(ctx);
        when(context.context().header(anyString())).thenReturn(null);
        final Result r = Results.ok("Result");
        when(context.proceed()).thenReturn(r);

        Result result = interceptor.call(cached, context);
        assertThat(result.getRenderable().<String>content()).isEqualTo("Result");
        assertThat(result).isEqualTo(r);
        // Check that the result was put in cache.
        verify(interceptor.cache, times(1)).get("/my/url?withquery");
        verify(interceptor.cache, times(1)).set("/my/url?withquery", r, Duration.standardSeconds(10));

        when(interceptor.cache.get("key")).thenReturn(r);
        result = interceptor.call(cached, context);
        assertThat(result).isEqualTo(r);

        verify(interceptor.cache, times(2)).get("/my/url?withquery");
    }

    @Test
    public void testCachingNoCache() throws Exception {
        CachedActionInterceptor interceptor = new CachedActionInterceptor();
        interceptor.cache = new DummyCache();
        Cached cached = mock(Cached.class);
        when(cached.duration()).thenReturn(10);
        when(cached.key()).thenReturn("key");

        RequestContext context = mock(RequestContext.class);
        when(context.request()).thenReturn(mock(Request.class));
        Context ctx = mock(Context.class);
        when(context.context()).thenReturn(ctx);
        when(context.context().header(anyString())).thenReturn(null);
        final Result r = Results.ok("Result");
        when(context.proceed()).thenReturn(r);

        Result result = interceptor.call(cached, context);

        assertThat(result.getRenderable().<String>content()).isEqualTo("Result");
        assertThat(result).isEqualTo(r);

        final Result r2 = Results.ok("Result2");
        when(context.proceed()).thenReturn(r2);

        result = interceptor.call(cached, context);
        // r is cached return r even is r2 is the new result.
        assertThat(result).isEqualTo(r);

        // The object is cached, let's use NO CACHE
        when(context.context().header(HeaderNames.CACHE_CONTROL)).thenReturn(HeaderNames.NOCACHE_VALUE);

        result = interceptor.call(cached, context);
        assertThat(result).isNotEqualTo(r).isEqualTo(r2);

        final Result r3 = Results.ok("Result3");
        when(context.proceed()).thenReturn(r3);

        // Remove the cache-control
        when(context.context().header(HeaderNames.CACHE_CONTROL)).thenReturn(null);
        result = interceptor.call(cached, context);
        assertThat(result).isEqualTo(r2).isNotEqualTo(r3);
    }

    @Test
    @Ignore("Does not reproduce the race condition")
    public void testPeak() throws InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        final EhCacheService svc = new EhCacheService();
        svc.configuration = configuration;
        svc.start();

        final CachedActionInterceptor interceptor = new CachedActionInterceptor();
        interceptor.cache = svc;
        final Cached cached = mock(Cached.class);
        when(cached.duration()).thenReturn(10);
        when(cached.key()).thenReturn("key");

        CountDownLatch startSignal = new CountDownLatch(1);
        final int client = 100;
        final CountDownLatch doneSignal = new CountDownLatch(client);
        ExecutorService executor = Executors.newFixedThreadPool(client);
        final AtomicInteger counter = new AtomicInteger();

        for (int i = 1; i < client + 1; ++i) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        RequestContext context = mock(RequestContext.class);
                        when(context.request()).thenReturn(mock(Request.class));
                        Context ctx = mock(Context.class);
                        when(context.context()).thenReturn(ctx);
                        when(context.context().header(anyString())).thenReturn(null);
                        final Result r = Results.ok("Result");
                        when(context.proceed()).thenReturn(r);
                        Result result = interceptor.call(cached, context);

                        if (! result.getRenderable().content().equals("Result")) {
                            counter.getAndIncrement();
                        }
                    } catch (Exception e) {
                        counter.getAndIncrement();
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

    private class DummyCache extends TreeMap<String, Object> implements Cache {
        @Override
        public void set(String key, Object value, int expiration) {
            put(key, value);
        }

        @Override
        public void set(String key, Object value, Duration expiration) {
            put(key, value);
        }

        @Override
        public Object get(String key) {
            return super.get(key);
        }

        @Override
        public boolean remove(String key) {
            return super.remove(key) != null;
        }
    }
}
