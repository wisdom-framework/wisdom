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
package org.wisdom.content.bodyparsers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wisdom.api.content.BodyParser;
import org.wisdom.api.content.Xml;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Component
@Provides
@Instantiate
public class BodyParserXML implements BodyParser {

    @Requires
    Xml xml;

    private static final String ERROR = "Error parsing incoming XML";

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyParserXML.class);

    public <T> T invoke(Context context, Class<T> classOfT) {
        T t = null;
        try {
            final String content = context.body();
            if (content == null || content.length() == 0) {
                return null;
            }
            if (classOfT.equals(Document.class)) {
                return (T) parseXMLDocument(content.getBytes(Charsets.UTF_8));
            }
            t = xml.xmlMapper().readValue(content, classOfT);
        } catch (IOException e) {
            LOGGER.error(ERROR, e);
        }

        return t;
    }

    @Override
    public <T> T invoke(byte[] bytes, Class<T> classOfT) {
        T t = null;
        try {
            if (classOfT.equals(Document.class)) {
                return (T) parseXMLDocument(bytes);
            }
            t = xml.xmlMapper().readValue(bytes, classOfT);
        } catch (IOException e) {
            LOGGER.error(ERROR, e);
        }

        return t;
    }

    private Document parseXMLDocument(byte[] bytes) {
        ByteArrayInputStream stream = null;
        try {
            stream = new ByteArrayInputStream(bytes);
            return xml.fromInputStream(stream, Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(ERROR, e);
            return null;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public List<String> getContentTypes() {
        return ImmutableList.of(MimeTypes.XML, "text/xml", "application/atom+xml");
    }

}
