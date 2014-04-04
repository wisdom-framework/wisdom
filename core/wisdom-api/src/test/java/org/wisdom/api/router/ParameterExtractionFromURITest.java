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
package org.wisdom.api.router;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the conversion of uri to regex
 */
public class ParameterExtractionFromURITest {

    @Test
    public void testNoParameter() {
        String uri = "/foo";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).isEmpty();
    }



    @Test
    public void testURIWithOnePathParameter() {
        String uri = "/foo/{id}";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }

    @Test
    public void testURIWithTwoPathParameters() {
        String uri = "/foo/{id}/{name}";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
        assertThat(list).contains("name");
    }

    @Test
    public void testURIWithDynamicPartSpanningOnSeveralSegments() {
        String uri = "/foo/*";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).isEmpty();
    }

    @Test
    public void testURIWithDynamicPartSpanningOnSeveralSegmentsIncludingRoot() {
        String uri = "/foo*";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).isEmpty();
    }

    @Test
    public void testURIMixingPathParametersAndDynamicParts() {
        String uri = "/foo/{id}/*";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }

    @Test
    public void testURIContainingRegexThatIsNotAParameter() {
        String uri = "/foo/[0-9]+";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).isEmpty();
    }

    /**
     * Test the {id<regex>} syntax
     */
    @Test
    public void testURIContainingRegexThatIsAParameter() {
        String uri = "/foo/{id<[0-9]+>}/{name}";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
        assertThat(list).contains("name");
    }

    /**
     * Test the {id*} syntax
     */
    @Test
    public void testURIContainingOptionalParameterSpanningOnSeveralSegments() {
        String uri = "/foo/{id*}";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }

    /**
     * Test the {id*} syntax
     */
    @Test
    public void testURIContainingParameterSpanningOnSeveralSegments() {
        String uri = "/foo/{id+}";
        List<String> list = RouteUtils.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }


}
