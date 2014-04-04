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
import org.junit.Test;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.cache.Cached;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.RequestContext;

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
}
