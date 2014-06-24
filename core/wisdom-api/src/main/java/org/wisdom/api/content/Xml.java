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
package org.wisdom.api.content;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A service interface used to handle XML objects and String.
 * It is basically a layer on top of Jackson. Be aware that implementation should support the dynamic addition and
 * removal of serializer/deserializer, making this service the main entry point to the XML support.
 */
public interface Xml {

    /**
     * Gets the current XML mapper.
     *
     * @return the mapper
     */
    public XmlMapper xmlMapper();

    /**
     * Builds a new XML Document from the given (xml) string.
     * By default this method uses UTF-8. If your document does not use UTF-8,
     * use {@link #fromInputStream(java.io.InputStream, java.nio.charset.Charset)} that let you set the encoding.
     *
     * @param xml the xml to parse, must not be {@literal null}
     * @return the document
     * @throws java.io.IOException if the given string is not a valid XML document
     */
    public Document fromString(String xml) throws IOException;

    /**
     * Builds a new XML Document from the given input stream. The stream is not closed by this method,
     * and so you must close it.
     *
     * @param stream   the input stream, must not be {@literal null}
     * @param encoding the encoding, if {@literal null}, UTF-8 is used.
     * @return the built document
     * @throws java.io.IOException if the given stream is not a valid XML document,
     *                             or if the given encoding is not supported.
     */
    Document fromInputStream(InputStream stream, Charset encoding) throws IOException;

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given XML document.
     *
     * @param document the XML document
     * @param clazz    the class of the instance to construct
     * @return an instance of the class.
     */
    <A> A fromXML(Document document, Class<A> clazz);

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given XML string.
     *
     * @param xml   the XML string
     * @param clazz the class of the instance to construct
     * @return an instance of the class.
     */
    <A> A fromXML(String xml, Class<A> clazz);

    /**
     * Retrieves the string form of the given XML document.
     *
     * @param xml the XML document, must not be {@literal null}
     * @return the String form of the object
     */
    String stringify(Document xml);

    /**
     * Creates a new document.
     *
     * @return the new document
     */
    Document newDocument();


}
