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
package org.wisdom.api.bodies;

import com.google.common.base.Charsets;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wisdom.api.http.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * A renderable object taking an Document as parameter.
 */
public class RenderableXML implements Renderable<Document> {

    private final Document document;
    private byte[] rendered;

    public RenderableXML(Document doc) {
        this.document = doc;
    }

    public RenderableXML(Element element) {
        this(element.getOwnerDocument());
    }

    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        if (rendered == null) {
            render();
        }
        return new ByteArrayInputStream(rendered);
    }

    private void render() throws RenderableException {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(document), new StreamResult(sw));
            rendered = sw.toString().getBytes(Charsets.UTF_8);
        } catch (Exception ex) {
            throw new RenderableException("Error converting XML document to String", ex);
        }
    }

    @Override
    public long length() {
        if (rendered == null) {
            try {
                render();
            } catch (RenderableException e) {  //NOSONAR
                LoggerFactory.getLogger(RenderableXML.class).warn("Cannot render XML object {}", document, e);
                return -1;
            }
        }
        return rendered.length;
    }

    @Override
    public String mimetype() {
        return MimeTypes.XML;
    }

    @Override
    public Document content() {
        return document;
    }

    @Override
    public boolean requireSerializer() {
        return false;
    }

    @Override
    public void setSerializedForm(String serialized) {
        // Nothing because serialization is not supported for this renderable class.
    }

    @Override
    public boolean mustBeChunked() {
        return false;
    }

}
