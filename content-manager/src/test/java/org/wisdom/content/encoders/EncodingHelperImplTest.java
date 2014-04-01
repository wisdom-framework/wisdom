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
package org.wisdom.content.encoders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.Controller;
import org.wisdom.api.bodies.RenderableString;
import org.wisdom.api.bodies.RenderableURL;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.EncodingNames;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.utils.KnownMimeTypes;
import org.wisdom.content.encoding.ContentEncodingHelperImpl;
import org.wisdom.content.encoding.ValuedEncoding;
import org.wisdom.test.parents.FakeContext;

public class EncodingHelperImplTest{

    ContentEncodingHelperImpl encodingHelper = null;

    ApplicationConfiguration configuration = null;

    @Before
    public void before(){	
        encodingHelper = new ContentEncodingHelperImpl();
        configuration = Mockito.mock(ApplicationConfiguration.class);	
        when(configuration.getBooleanWithDefault(anyString(), anyBoolean())).then(new Answer<Boolean>() {

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return (Boolean) invocation.getArguments()[1];
            }
        });
        when(configuration.getLongWithDefault(anyString(), anyLong())).then(new Answer<Long>() {

            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return (Long)invocation.getArguments()[1];
            }
        });
        when(configuration.getIntegerWithDefault(anyString(), anyInt())).then(new Answer<Integer>() {

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return (Integer)invocation.getArguments()[1];
            }
        });
        encodingHelper.setConfiguration(configuration);
    }

    @Test
    public void testShouldEncodeWithHeaders(){
        Map<String, String> headers = new HashMap<String, String>();
        //Already encoded
        headers.put(HeaderNames.CONTENT_ENCODING, EncodingNames.GZIP);
        assertThat(encodingHelper.shouldEncodeWithHeaders(headers)).isEqualTo(false);

        headers.clear();
        //Content_Encoding set but empty
        headers.put(HeaderNames.CONTENT_ENCODING, "");
        assertThat(encodingHelper.shouldEncodeWithHeaders(headers)).isEqualTo(true);

        headers.clear();
        //Content_Encoding set but Identity
        headers.put(HeaderNames.CONTENT_ENCODING, EncodingNames.IDENTITY);
        assertThat(encodingHelper.shouldEncodeWithHeaders(headers)).isEqualTo(true);

        headers.clear();
        //Content_Encoding set but null
        headers.put(HeaderNames.CONTENT_ENCODING, EncodingNames.IDENTITY);
        assertThat(encodingHelper.shouldEncodeWithHeaders(headers)).isEqualTo(true);

        headers.clear();
        //Content_Encoding not set
        assertThat(encodingHelper.shouldEncodeWithHeaders(headers)).isEqualTo(true);

        headers.clear();
        //Null parameter
        assertThat(encodingHelper.shouldEncodeWithHeaders(null)).isEqualTo(true);
    }

    @Test
    public void testShouldEncodeWithMimeType(){
        RenderableURL renderable = Mockito.mock(RenderableURL.class);

        //Test all known Mime type
        for(String mime : KnownMimeTypes.EXTENSIONS.values()){
            when(renderable.mimetype()).thenReturn(mime);
            if(KnownMimeTypes.COMPRESSED_MIME.contains(mime))
                assertThat(encodingHelper.shouldEncodeWithMimeType(renderable)).isEqualTo(false);
            else
                assertThat(encodingHelper.shouldEncodeWithMimeType(renderable)).isEqualTo(true);
        }

        //Null mimetype
        when(renderable.mimetype()).thenReturn(null);
        assertThat(encodingHelper.shouldEncodeWithMimeType(renderable)).isEqualTo(false);

        //Null renderable
        assertThat(encodingHelper.shouldEncodeWithMimeType(null)).isEqualTo(false);
    }

    @Test
    public void testShouldEncodeWithSizeWithoutRoutes(){
        RenderableString renderable = Mockito.mock(RenderableString.class);

        //out of bounds
        when(renderable.length()).thenReturn(Long.MIN_VALUE);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(false);

        when(renderable.length()).thenReturn(Long.MAX_VALUE);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(false);

        when(renderable.length()).thenReturn(0L);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(false);

        when(renderable.length()).thenReturn(50L);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(false);

        when(renderable.length()).thenReturn(ApplicationConfiguration.DEFAULT_ENCODING_MAX_SIZE + 1);
        when(renderable.length()).thenReturn(Long.MIN_VALUE);

        when(renderable.length()).thenReturn(ApplicationConfiguration.DEFAULT_ENCODING_MIN_SIZE - 1);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(false);

        //inside bounds
        when(renderable.length()).thenReturn(1024 * 50L);// 50Ko
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(true);

        when(renderable.length()).thenReturn(ApplicationConfiguration.DEFAULT_ENCODING_MAX_SIZE);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(true);

        when(renderable.length()).thenReturn(ApplicationConfiguration.DEFAULT_ENCODING_MIN_SIZE);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(true);

        //Shouldn't encode -1 length
        when(renderable.length()).thenReturn(-1L);
        assertThat(encodingHelper.shouldEncodeWithSize(null, renderable)).isEqualTo(false);

        //Null renderable
        assertThat(encodingHelper.shouldEncodeWithSize(null, null)).isEqualTo(false);
    }

    @Test
    public void testShouldEncodeWithSizeWithRoutes(){
        RenderableString renderable = Mockito.mock(RenderableString.class);
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(new FakeSizeController(), "changeSize");

        //out of bounds
        when(renderable.length()).thenReturn(Long.MIN_VALUE);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(false);

        when(renderable.length()).thenReturn(Long.MAX_VALUE);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(false);

        when(renderable.length()).thenReturn(1L - 1);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(false);

        when(renderable.length()).thenReturn(1000000*1024L + 1);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(false);

        //in annotation bounds
        when(renderable.length()).thenReturn(1L);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(true);

        when(renderable.length()).thenReturn(1000000*1024L);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(true);

        when(renderable.length()).thenReturn(1L + 1);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(true);

        when(renderable.length()).thenReturn(1000000*1024L - 1);
        assertThat(encodingHelper.shouldEncodeWithSize(route, renderable)).isEqualTo(true);
    }

    @Test
    public void testShouldEncodeWithRouteDefaultTrue(){
        //Default is true !
        Controller deny = new FakeDenyController();
        Route routeDenyDeny = new RouteBuilder().route(HttpMethod.GET).on("/").to(deny, "deny");
        Route routeDenyAllow = new RouteBuilder().route(HttpMethod.GET).on("/").to(deny, "allow");
        Route routeDenyNo = new RouteBuilder().route(HttpMethod.GET).on("/").to(deny, "noAnnotation");

        assertThat(encodingHelper.shouldEncodeWithRoute(routeDenyDeny)).isEqualTo(false);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeDenyAllow)).isEqualTo(true);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeDenyNo)).isEqualTo(false);

        Controller allow = new FakeAllowController();
        Route routeAllowDeny = new RouteBuilder().route(HttpMethod.GET).on("/").to(allow, "deny");
        Route routeAllowAllow = new RouteBuilder().route(HttpMethod.GET).on("/").to(allow, "allow");
        Route routeAllowNo = new RouteBuilder().route(HttpMethod.GET).on("/").to(allow, "noAnnotation");

        assertThat(encodingHelper.shouldEncodeWithRoute(routeAllowDeny)).isEqualTo(false);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeAllowAllow)).isEqualTo(true);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeAllowNo)).isEqualTo(true);

        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(new FakeSizeController(), "noAnnotation");

        assertThat(encodingHelper.shouldEncodeWithRoute(route)).isEqualTo(true);

        assertThat(encodingHelper.shouldEncodeWithRoute(route)).isEqualTo(true);
    }

    @Test
    public void testShouldEncodeWithRouteDefaultFalse(){
        //Default is false
        when(configuration.getBooleanWithDefault(anyString(), anyBoolean())).then(new Answer<Boolean>() {

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return false;
            }
        });
        Controller deny = new FakeDenyController();
        Route routeDenyDeny = new RouteBuilder().route(HttpMethod.GET).on("/").to(deny, "deny");
        Route routeDenyAllow = new RouteBuilder().route(HttpMethod.GET).on("/").to(deny, "allow");
        Route routeDenyNo = new RouteBuilder().route(HttpMethod.GET).on("/").to(deny, "noAnnotation");

        assertThat(encodingHelper.shouldEncodeWithRoute(routeDenyDeny)).isEqualTo(false);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeDenyAllow)).isEqualTo(true);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeDenyNo)).isEqualTo(false);

        Controller allow = new FakeAllowController();
        Route routeAllowDeny = new RouteBuilder().route(HttpMethod.GET).on("/").to(allow, "deny");
        Route routeAllowAllow = new RouteBuilder().route(HttpMethod.GET).on("/").to(allow, "allow");
        Route routeAllowNo = new RouteBuilder().route(HttpMethod.GET).on("/").to(allow, "noAnnotation");

        assertThat(encodingHelper.shouldEncodeWithRoute(routeAllowDeny)).isEqualTo(false);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeAllowAllow)).isEqualTo(true);
        assertThat(encodingHelper.shouldEncodeWithRoute(routeAllowNo)).isEqualTo(true);

        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(new FakeSizeController(), "noAnnotation");
        assertThat(encodingHelper.shouldEncodeWithRoute(route)).isEqualTo(false);

        assertThat(encodingHelper.shouldEncodeWithRoute(null)).isEqualTo(false);
    }

    @Test
    public void testShouldEncodeParameters(){
        assertThat(encodingHelper.shouldEncode(null, new Result(), new RenderableString(""))).isFalse();
        assertThat(encodingHelper.shouldEncode(null, null, new RenderableString(""))).isFalse();
        assertThat(encodingHelper.shouldEncode(new FakeContext(), null, new RenderableString(""))).isFalse();
    }

    @Test
    public void testGetEncodingGlobalSettings(){
        assertThat(encodingHelper.getAllowUrlEncodingGlobalSetting()).isTrue();
    }

    @Test
    public void testParseAcceptEncoding(){
        assertThat(encodingHelper.parseAcceptEncodingHeader(null).size()).isEqualTo(0);
        assertThat(encodingHelper.parseAcceptEncodingHeader("  ").size()).isEqualTo(0);
        assertThat(encodingHelper.parseAcceptEncodingHeader("\n").size()).isEqualTo(0);
        //q=0 encoding are removed
        assertThat(encodingHelper.parseAcceptEncodingHeader("gzip;q=0").size()).isEqualTo(0);
        //sorting encodings
        List<String> results = encodingHelper.parseAcceptEncodingHeader("gzip;q=0.2, deflate");
        assertThat(results.get(0)).isEqualTo("deflate");
        assertThat(results.get(1)).isEqualTo("gzip");
    }
    
    @Test
    public void testValuedEncoding(){
        ValuedEncoding v = new ValuedEncoding("gzip;q=0.5", 1);
        assertThat(v.getPosition()).isEqualTo(1);
        assertThat(v.getEncoding()).isEqualTo("gzip");
        assertThat(v.getqValue()).isEqualTo(0.5);
        
        ValuedEncoding v2 = new ValuedEncoding("gzip;q=0.6", 1);
        assertThat(v.compareTo(v2)).isEqualTo(1);
        
        ValuedEncoding v3 = new ValuedEncoding("gzip;q=0.5", 2);
        assertThat(v.compareTo(v3)).isEqualTo(-1);
        
        ValuedEncoding v4 = new ValuedEncoding("gzip;q=0.5", 1);
        assertThat(v.equals(v4)).isTrue();
        assertThat(v.equals(v3)).isFalse();
    }
}
