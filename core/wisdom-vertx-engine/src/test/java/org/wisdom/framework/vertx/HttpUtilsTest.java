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
package org.wisdom.framework.vertx;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import org.junit.Test;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpUtilsTest {

    @Test
    public void testIsKeepAlive() throws Exception {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        HttpServerRequest req = mock(HttpServerRequest.class);
        when(req.headers()).thenReturn(headers);

        // Connection header set.
        headers.add(HeaderNames.CONNECTION, HttpUtils.CLOSE);
        assertThat(HttpUtils.isKeepAlive(req)).isFalse();

        headers.add(HeaderNames.CONNECTION, HttpUtils.KEEP_ALIVE);
        assertThat(HttpUtils.isKeepAlive(req)).isTrue();

        // Unset connection header
        headers.clear();
        when(req.version()).thenReturn(HttpVersion.HTTP_1_1);
        assertThat(HttpUtils.isKeepAlive(req)).isTrue();

        when(req.version()).thenReturn(HttpVersion.HTTP_1_0);
        assertThat(HttpUtils.isKeepAlive(req)).isFalse();
    }

    @Test
    public void testGetStatusFromResult() throws Exception {
        assertThat(HttpUtils.getStatusFromResult(new Result(Status.OK), false)).isEqualTo(Status.BAD_REQUEST);
        assertThat(HttpUtils.getStatusFromResult(new Result(Status.OK), true)).isEqualTo(Status.OK);
    }

    @Test
    public void testGetContentTypeFromContentTypeAndCharacterSetting() throws Exception {
        assertThat(HttpUtils.getContentTypeFromContentTypeAndCharacterSetting("application/json; charset=\"utf-8\""))
                .isEqualTo("application/json");
        assertThat(HttpUtils.getContentTypeFromContentTypeAndCharacterSetting("application/json"))
                .isEqualTo("application/json");
    }

    @Test
    public void testIsPostOrPut() throws Exception {
        HttpServerRequest req = mock(HttpServerRequest.class);
        when(req.method()).thenReturn(HttpMethod.POST);
        assertThat(HttpUtils.isPostOrPut(req)).isTrue();
        when(req.method()).thenReturn(HttpMethod.PUT);
        assertThat(HttpUtils.isPostOrPut(req)).isTrue();
        when(req.method()).thenReturn(HttpMethod.GET);
        assertThat(HttpUtils.isPostOrPut(req)).isFalse();
    }
}