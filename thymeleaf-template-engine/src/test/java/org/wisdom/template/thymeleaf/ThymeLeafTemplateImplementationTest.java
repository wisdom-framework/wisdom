package org.wisdom.template.thymeleaf;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;


/**
 * checks the Template implementation for Thymeleaf
 *
 * bundle:// cannot be used here as the url handler is not registered. Use file instead.
 */
public class ThymeLeafTemplateImplementationTest {

    @Test
    public void testNameExtractionFromURL() throws MalformedURLException {
        URL url = new URL("file://38.0:0/templates/footer.html");
        ThymeLeafTemplateImplementation template = new ThymeLeafTemplateImplementation(null, url, null);
        assertThat(template.fullName()).isEqualTo(url.toExternalForm());
        assertThat(template.name()).isEqualTo("footer");
    }

    @Test
    public void testNameExtractionFromURLUsingSubFolder() throws MalformedURLException {
        URL url = new URL("file://38.0:0/templates/hello/footer.html");
        ThymeLeafTemplateImplementation template = new ThymeLeafTemplateImplementation(null, url, null);
        assertThat(template.fullName()).isEqualTo(url.toExternalForm());
        assertThat(template.name()).isEqualTo("hello/footer");
    }

}
