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

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the conversion of uri to regex
 */
public class RawUriToRegexConversionTest {

    @Test
    public void testURIWithoutVariability() {
        String uri = "/foo";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isTrue();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIWithOnePathParameter() {
        String uri = "/foo/{id}";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/foo/").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isTrue();
        assertThat(pattern.matcher("/foo/x/y").matches()).isFalse();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIWithTwoPathParameters() {
        String uri = "/foo/{id}/{name}";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x/y").matches()).isTrue();
        assertThat(pattern.matcher("/foo/x/y/z").matches()).isFalse();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIWithTwoPathParametersInTheSameSegment() {
        String uri = "/foo/{id}-{name}";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x-y").matches()).isTrue();
        assertThat(pattern.matcher("/foo/x/y").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x-y/z").matches()).isFalse();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIWithDynamicPartSpanningOnSeveralSegments() {
        String uri = "/foo/*";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isTrue();
        assertThat(pattern.matcher("/foo/x/y").matches()).isTrue();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIWithDynamicPartSpanningOnSeveralSegmentsIncludingRoot() {
        String uri = "/foo*";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isTrue();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isTrue();
        assertThat(pattern.matcher("/foo/x/y").matches()).isTrue();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIMixingPathParametersAndDynamicParts() {
        String uri = "/foo/{id}/*";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x/y").matches()).isTrue();
        assertThat(pattern.matcher("/foo/x/y/z").matches()).isTrue();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIContainingRegexThatIsNotAParameter() {
        String uri = "/foo/[0-9]+";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/foo/12354").matches()).isTrue();
        assertThat(pattern.matcher("/foo/12345/y").matches()).isFalse();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    /**
     * Test the {id<regex>} syntax
     */
    @Test
    public void testURIContainingRegexThatIsAParameter() {
        String uri = "/foo/{id<[0-9]+>}/{name}";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/foo/12354").matches()).isFalse();
        assertThat(pattern.matcher("/foo/12345/y").matches()).isTrue();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    /**
     * Test the {id<regex>} syntax but spanning on several segments.
     */
    @Test
    public void testURIContainingRegexThatIsAParameterSpanningOnSeveralSegments() {
        String uri = "/foo/{path<.+>}";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isTrue();
        assertThat(pattern.matcher("/foo/12354").matches()).isTrue();
        assertThat(pattern.matcher("/foo/12345/y").matches()).isTrue();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    /**
     * Test the {id*} syntax but spanning on several segments.
     */
    @Test
    public void testURIContainingAParameterSpanningOnSeveralSegments() {
        String uri = "/foo/{path*}";
        String regex = RouteUtils.convertRawUriToRegex(uri);

        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isTrue();
        assertThat(pattern.matcher("/foo/12354").matches()).isTrue();
        assertThat(pattern.matcher("/foo/12345/y").matches()).isTrue();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    /**
     * Test the {x} in regex
     */
    @Test
    public void testURIContainingRegexWithBraces() {
        String uri = "/foo/{id<[a-z]{2}>}";
        String regex = RouteUtils.convertRawUriToRegex(uri);
        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/foo/xy").matches()).isTrue();
        assertThat(pattern.matcher("/foo/xY").matches()).isFalse();
        assertThat(pattern.matcher("/foo/xyz").matches()).isFalse();
        assertThat(pattern.matcher("/foo/xy/z").matches()).isFalse();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test
    public void testURIContainingRegexWithBracesFollowedByStar() {
        String uri = "/foo/{id<[a-z]{2}>}*";
        String regex = RouteUtils.convertRawUriToRegex(uri);
        Pattern pattern = Pattern.compile(regex);
        assertThat(pattern.matcher("/foo").matches()).isFalse();
        assertThat(pattern.matcher("/bar").matches()).isFalse();
        assertThat(pattern.matcher("/foo/x").matches()).isFalse();
        assertThat(pattern.matcher("/foo/xy").matches()).isTrue();
        assertThat(pattern.matcher("/foo/xY").matches()).isFalse();
        assertThat(pattern.matcher("/foo/xyz").matches()).isTrue();
        assertThat(pattern.matcher("/foo/xy/z").matches()).isTrue();
        assertThat(pattern.matcher("/").matches()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotClosedVariable() {
        String uri = "/foo/{xxx/";
        String regex = RouteUtils.convertRawUriToRegex(uri);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonClosedRegex() {
        String uri = "/foo/{xxx<[a-z]+}";
        String regex = RouteUtils.convertRawUriToRegex(uri);
    }


}
