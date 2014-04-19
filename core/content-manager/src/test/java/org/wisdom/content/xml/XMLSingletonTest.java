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
package org.wisdom.content.xml;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wisdom.content.jackson.JacksonSingleton;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the XML support.
 */
public class XMLSingletonTest {

    JacksonSingleton xml = new JacksonSingleton();

    @Before
    public void setUp() {
        xml.validate();
    }

    @Test
    public void testMapper() throws Exception {
        assertThat(xml.xmlMapper()).isNotNull();
    }

    @Test
    public void testFromString() throws Exception {
        String txt = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<data lg=\"english\" version=\"1\">\n" +
                "<message>Hello</message>\n" +
                "<message>Welcome</message>\n" +
                "</data>\n";

        Document document = xml.fromString(txt);
        assertThat(document.getDocumentElement().getAttribute("lg")).isEqualTo("english");
        assertThat(document.getDocumentElement().getAttribute("version")).isEqualTo("1");
        assertThat(document.getDocumentElement().getElementsByTagName("message").getLength()).isEqualTo(2);
    }

    @Test
    public void testFromInputStream() throws Exception {
        String txt = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<data lg=\"english\" version=\"1\">\n" +
                "<message>Hello</message>\n" +
                "<message>Welcome</message>\n" +
                "</data>\n";

        InputStream stream = new ByteArrayInputStream(txt.getBytes(Charsets.UTF_8));
        Document document = xml.fromInputStream(stream, Charsets.UTF_8);
        assertThat(document.getDocumentElement().getAttribute("lg")).isEqualTo("english");
        assertThat(document.getDocumentElement().getAttribute("version")).isEqualTo("1");
        assertThat(document.getDocumentElement().getElementsByTagName("message").getLength()).isEqualTo(2);
        IOUtils.closeQuietly(stream);

        // Same without charset.
        stream = new ByteArrayInputStream(txt.getBytes(Charsets.UTF_8));
        document = xml.fromInputStream(stream, null);
        assertThat(document.getDocumentElement().getAttribute("lg")).isEqualTo("english");
        assertThat(document.getDocumentElement().getAttribute("version")).isEqualTo("1");
        assertThat(document.getDocumentElement().getElementsByTagName("message").getLength()).isEqualTo(2);
        IOUtils.closeQuietly(stream);
    }

    @Test
    public void testFromXMLUsingDocument() throws Exception {
        Document document = xml.newDocument();
        Element root = document.createElement("data");
        root.setAttribute("version", "1");
        root.setAttribute("lg", "english");
        Element message = document.createElement("message");
        message.setTextContent("Hello");
        root.appendChild(message);
        document.appendChild(root);

        Data data = xml.fromXML(document, Data.class);
        assertThat(data.lg).isEqualTo("english");
        assertThat(data.version).isEqualTo(1);
        assertThat(data.message.message).isEqualTo("Hello");
    }

    @Test
    public void testFromXMLUsingString() throws Exception {
        String txt = "<data version=\"1\" lg=\"en\">" +
                "<message>Hello</message></data>";

        Data data = xml.fromXML(txt, Data.class);
        assertThat(data.lg).isEqualTo("en");
        assertThat(data.version).isEqualTo(1);
        assertThat(data.message.message).isEqualTo("Hello");
    }

    @Test
    public void testStringify() throws Exception {
        Document document = xml.newDocument();
        Element root = document.createElement("data");
        root.setAttribute("version", "1");
        root.setAttribute("lg", "english");
        Element message = document.createElement("message");
        message.setTextContent("Hello");
        Element message2 = document.createElement("message");
        message2.setTextContent("Welcome");
        root.appendChild(message);
        root.appendChild(message2);
        document.appendChild(root);

        assertThat(xml.stringify(document))
                .contains("?xml")
                .contains("UTF-8")
                .contains("<data")
                .contains("lg=\"english\"")
                .contains("<message>Hello</message>")
                .contains("<message>Welcome</message")
                .contains("</data>");
    }
}
