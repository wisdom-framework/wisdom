package org.ow2.chameleon.wisdom.router;

import org.junit.Test;
import org.ow2.chameleon.wisdom.api.route.Route;

import java.util.List;
import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Checks the conversion of uri to regex
 */
public class ParameterExtractionFromURITest {

    @Test
    public void testNoParameter() {
        String uri = "/foo";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list.isEmpty());
    }

    @Test
    public void testURIWithOnePathParameter() {
        String uri = "/foo/{id}";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }

    @Test
    public void testURIWithTwoPathParameters() {
        String uri = "/foo/{id}/{name}";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
        assertThat(list).contains("name");
    }

    @Test
    public void testURIWithDynamicPartSpanningOnSeveralSegments() {
        String uri = "/foo/*";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).isEmpty();
    }

    @Test
    public void testURIWithDynamicPartSpanningOnSeveralSegmentsIncludingRoot() {
        String uri = "/foo*";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).isEmpty();
    }

    @Test
    public void testURIMixingPathParametersAndDynamicParts() {
        String uri = "/foo/{id}/*";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }

    @Test
    public void testURIContainingRegexThatIsNotAParameter() {
        String uri = "/foo/[0-9]+";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).isEmpty();
    }

    /**
     * Test the {id<regex>} syntax
     */
    @Test
    public void testURIContainingRegexThatIsAParameter() {
        String uri = "/foo/{id<[0-9]+>}/{name}";
        List<String> list = Route.extractParameters(uri);
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
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }

    /**
     * Test the {id*} syntax
     */
    @Test
    public void testURIContainingParameterSpanningOnSeveralSegments() {
        String uri = "/foo/{id+}";
        List<String> list = Route.extractParameters(uri);
        System.out.println(uri + " => " + list);

        assertThat(list).contains("id");
    }


}
