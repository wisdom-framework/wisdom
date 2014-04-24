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
package router;


import com.google.common.collect.ImmutableMap;
import controllers.HelloController;
import controllers.ParameterController;
import controllers.UrlCodingController;
import org.junit.Test;
import org.wisdom.api.router.Router;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ReverseRouteIT extends WisdomTest {

    @Inject
    Router router;

    @Test
    public void testReverseRouteWithoutParams() {
        // Path + uri.
        assertThat(router.getReverseRouteFor(HelloController.class, "asText")).isEqualTo("/hello/plain");

        // We don't provide the value of the argument.
        assertThat(router.getReverseRouteFor(ParameterController.class, "takeLong")).isEqualTo("/parameter/long/{l}");
    }

    @Test
    public void testReverseRouteWithParams() {
        // Path + uri.
        assertThat(router.getReverseRouteFor(HelloController.class, "asText", Collections.<String, Object>emptyMap()))
                .isEqualTo("/hello/plain");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeLong",
                ImmutableMap.<String, Object>of("l", "1"))).isEqualTo("/parameter/long/1");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeLong",
                "l", "1")).isEqualTo("/parameter/long/1");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeLongFromQuery",
                "l", "1")).isEqualTo("/parameter/query/long?l=1");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeLongFromQuery",
                "l", "1", "a", "b")).contains("/parameter/query/long?").contains("l=1").contains("a=b");
    }

    @Test
    public void testReverseRoutingUsingBooleanParameters() {
        assertThat(router.getReverseRouteFor(ParameterController.class, "takeBoolean", "b",
                true)).isEqualTo("/parameter/boolean/true");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeBoolean", "b",
                false)).isEqualTo("/parameter/boolean/false");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeBooleanFromQuery", "b",
                true)).isEqualTo("/parameter/query/boolean?b=true");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeBooleanFromQuery", "b",
                false)).isEqualTo("/parameter/query/boolean?b=false");
    }

    @Test
    public void testReverseRoutingUsingIntegerParameters() {
        assertThat(router.getReverseRouteFor(ParameterController.class, "takeInt", "i",
                10)).isEqualTo("/parameter/integer/10");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeIntFromQuery", "i",
                10)).isEqualTo("/parameter/query/integer?i=10");
    }

    @Test
    public void testReverseRoutingUsingLongParameters() {
        assertThat(router.getReverseRouteFor(ParameterController.class, "takeLong", "l",
                10)).isEqualTo("/parameter/long/10");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeLongFromQuery", "l",
                10)).isEqualTo("/parameter/query/long?l=10");
    }

    @Test
    public void testReverseRoutingUsingStringParameters() {
        assertThat(router.getReverseRouteFor(ParameterController.class, "takeString", "s",
                "hello")).isEqualTo("/parameter/string/hello");

        assertThat(router.getReverseRouteFor(ParameterController.class, "takeStringFromQuery", "s",
                "hello")).isEqualTo("/parameter/query/string?s=hello");
    }

    @Test
    public void testURLEncoding() throws Exception {
        checkEncoding("123", "456", "789", "123", "456", "789");
        checkEncoding("+", "+", "+", "+",   "+",   "%2B");
        checkEncoding(" ", " ", " ", "%20", "%20", "+");
        checkEncoding("&", "&", "&", "&",   "&",   "%26");
        checkEncoding("=", "=", "=", "=",   "=",   "%3D");
        checkEncoding("/", "/", "/", "%2F", "%2F",   "%2F");
        checkEncoding("~", "~", "~", "~",   "~",   "%7E");

    }

    public void checkEncoding(String decoded1, String decoded2, String decoded3,
                             String encoded1, String encoded2, String encoded3) throws Exception {
        final String expected = "/urlcoding/" + encoded1 + "/" + encoded2 + "?q=" + encoded3;
        final String computed = router.getReverseRouteFor(UrlCodingController.class, "coding", "p1", decoded1, "p2",
                decoded2, "q", decoded3);
        assertThat(computed).isEqualTo(expected);

    }
}
