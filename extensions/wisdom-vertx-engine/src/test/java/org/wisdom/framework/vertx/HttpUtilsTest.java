package org.wisdom.framework.vertx;

import org.junit.Test;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpVersion;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpUtilsTest {

    @Test
    public void testIsKeepAlive() throws Exception {
        CaseInsensitiveMultiMap headers = new CaseInsensitiveMultiMap();
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
        when(req.method()).thenReturn("post");
        assertThat(HttpUtils.isPostOrPut(req)).isTrue();
        when(req.method()).thenReturn("put");
        assertThat(HttpUtils.isPostOrPut(req)).isTrue();
        when(req.method()).thenReturn("get");
        assertThat(HttpUtils.isPostOrPut(req)).isFalse();
    }
}