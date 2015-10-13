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
package org.wisdom.content.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.content.JacksonModuleRepository;
import org.wisdom.api.content.Json;
import org.wisdom.api.content.Xml;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * This component is a layer on top of Jackson and provides the {@link org.wisdom.api.content.Json}
 * and {@link org.wisdom.api.content.Xml} services.
 * <p/>
 * This class manages Jackson module dynamically, and recreates a JSON Mapper and XML mapper every time a module arrives
 * or leaves.
 */
@Component(immediate = true)
@Provides
@Instantiate
public class JacksonSingleton implements JacksonModuleRepository, Json, Xml {

    /**
     * An object used as lock.
     */
    private final Object lock = new Object();

    /**
     * The current object mapper.
     */
    private ObjectMapper mapper;

    /**
     * The current object mapper.
     */
    private XmlMapper xml;

    /**
     * The document builder factory used to create new document.
     */
    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonSingleton.class);

    /**
     * The current set of registered modules.
     */
    private Set<Module> modules = new HashSet<>();

    /**
     * The application configuration to read the jackson enabled / disabled features.
     */
    @Requires
    public ApplicationConfiguration configuration;

    /**
     * Creates a new instance of {@link JacksonSingleton}.
     */
    public JacksonSingleton() {
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            // Just logged even if it's quite important
            // Some parser do not support the option (and should probably not be used).
            LOGGER.error("Cannot use secure processing for XML document", e);
        }
    }

    /**
     * Gets the current mapper.
     *
     * @return the mapper.
     */
    public ObjectMapper mapper() {
        synchronized (lock) {
            return mapper;
        }
    }

    /**
     * Converts an object to JsonNode.
     *
     * @param data Value to convert in Json.
     * @return the resulting JSON Node
     * @throws java.lang.RuntimeException if the JSON Node cannot be created
     */
    public JsonNode toJson(final Object data) {
        synchronized (lock) {
            try {
                return mapper.valueToTree(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Gets the JSONP response for the given callback and value.
     *
     * @param callback the callback name
     * @param data     the data to transform to json
     * @return the String built as follows: "callback(json(data))"
     */
    public String toJsonP(final String callback, final Object data) {
        synchronized (lock) {
            try {
                return callback + "(" + stringify((JsonNode) mapper.valueToTree(data)) + ");";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Converts a JsonNode to a Java value.
     *
     * @param json  Json value to convert.
     * @param clazz Expected Java value type.
     * @return the created object
     * @throws java.lang.RuntimeException if the object cannot be created
     */
    public <A> A fromJson(JsonNode json, Class<A> clazz) {
        synchronized (lock) {
            try {
                return mapper.treeToValue(json, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Converts a Json String to a Java value.
     *
     * @param json  Json string to convert.
     * @param clazz Expected Java value type.
     * @return the created object
     * @throws java.lang.RuntimeException if the object cannot be created
     */
    public <A> A fromJson(String json, Class<A> clazz) {
        synchronized (lock) {
            try {
                JsonNode node = mapper.readTree(json);
                return mapper.treeToValue(node, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Converts a JsonNode to its string representation.
     * This implementation use a `pretty printer`.
     *
     * @param json the json node
     * @return the String representation of the given Json Object
     * @throws java.lang.RuntimeException if the String form cannot be created
     */
    public String stringify(JsonNode json) {
        try {
            return mapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot stringify the input json node", e);
        }
    }

    /**
     * Parses a String representing a json, and return it as a JsonNode.
     *
     * @param src the JSON String
     * @return the Json Node
     * @throws java.lang.RuntimeException if the given string is not a valid JSON String
     */
    public JsonNode parse(String src) {
        synchronized (lock) {
            try {
                return mapper.readValue(src, JsonNode.class);
            } catch (Exception t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Parses a stream representing a json, and return it as a JsonNode.
     * The stream is <strong>not</strong> closed by the method.
     *
     * @param stream the JSON stream
     * @return the JSON node
     * @throws java.lang.RuntimeException if the given stream is not a valid JSON String
     */
    public JsonNode parse(InputStream stream) {
        synchronized (lock) {
            try {
                return mapper.readValue(stream, JsonNode.class);
            } catch (Exception t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Creates a new JSON Object.
     *
     * @return the new Object Node.
     */
    @Override
    public ObjectNode newObject() {
        return mapper().createObjectNode();
    }

    /**
     * Creates a new JSON Array.
     *
     * @return the new Array Node.
     */
    @Override
    public ArrayNode newArray() {
        return mapper().createArrayNode();
    }

    /**
     * Starts the JSON and XML support.
     * An empty mapper is created.
     */
    @Validate
    public void validate() {
        LOGGER.info("Starting JSON and XML support services");
        setMappers(new ObjectMapper(), new XmlMapper());
    }

    /**
     * Sets the object mapper.
     *
     * @param mapper the object mapper to use
     */
    private void setMappers(ObjectMapper mapper, XmlMapper xml) {
        synchronized (lock) {
            this.mapper = mapper;
            this.xml = xml;
            // mapper and xml are set to null on invalidation.
            if (mapper != null && xml != null) {
                applyMapperConfiguration(mapper, xml);
            }
        }
    }

    private void applyMapperConfiguration(ObjectMapper mapper, XmlMapper xml) {
        Configuration conf = null;

        // Check for test.
        if (configuration != null) {
            conf = configuration.getConfiguration("jackson");
        }

        if (conf == null) {
            LOGGER.info("Using default (Wisdom) configuration of Jackson");
            LOGGER.info("FAIL_ON_UNKNOWN_PROPERTIES is disabled");
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            xml.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        } else {
            LOGGER.info("Applying custom configuration on Jackson mapper");
            Set<String> keys = conf.asMap().keySet();
            for (String key : keys) {
                setFeature(mapper, xml, key, conf.getBoolean(key));
            }
        }
    }

    private void setFeature(ObjectMapper mapper, XmlMapper xml, String key, Boolean value) {
        try {
            MapperFeature feature = MapperFeature.valueOf(key);
            mapper.configure(feature, value);
            xml.configure(feature, value);
            return;
        } catch (IllegalArgumentException e) {
            // Next attempt
        }

        try {
            DeserializationFeature feature = DeserializationFeature.valueOf(key);
            mapper.configure(feature, value);
            xml.configure(feature, value);
            return;
        } catch (IllegalArgumentException e) {
            // Next attempt
        }

        try {
            SerializationFeature feature = SerializationFeature.valueOf(key);
            mapper.configure(feature, value);
            xml.configure(feature, value);
            return;
        } catch (IllegalArgumentException e) {
            // Next attempt
        }

        try {
            JsonParser.Feature feature = JsonParser.Feature.valueOf(key);
            mapper.configure(feature, value);
            xml.configure(feature, value);
            return;
        } catch (IllegalArgumentException e) {
            // Next attempt
        }

        try {
            JsonGenerator.Feature feature = JsonGenerator.Feature.valueOf(key);
            mapper.configure(feature, value);
            xml.configure(feature, value);
            return;
        } catch (IllegalArgumentException e) {
            // There is no other attempts, but we catch it because we want to customize the error message.
        }

        throw new IllegalArgumentException("Cannot find a feature with the following name : " + key);

    }

    /**
     * Stops the JSON and XML management.
     * Releases the current mappers.
     */
    @Invalidate
    public void invalidate() {
        setMappers(null, null);
    }

    /**
     * Registers a new Jackson Module.
     *
     * @param module the module to register
     */
    @Override
    public void register(Module module) {
        if (module == null) {
            return;
        }
        LOGGER.info("Adding JSON module {}", module.getModuleName());
        synchronized (lock) {
            modules.add(module);
            rebuildMappers();
        }
    }

    private void rebuildMappers() {
        mapper = new ObjectMapper();
        for (Module module : modules) {
            mapper.registerModule(module);
        }

        xml = new XmlMapper();
        for (Module module : modules) {
            xml.registerModule(module);
        }

        applyMapperConfiguration(mapper, xml);
    }

    /**
     * Un-registers a JSON Module.
     *
     * @param module the module
     */
    @Override
    public void unregister(Module module) {
        if (module == null) {
            // May happen on departure.
            return;
        }
        LOGGER.info("Removing Jackson module {}", module.getModuleName());
        synchronized (lock) {
            if (modules.remove(module)) {
                rebuildMappers();
            }
        }
    }


    /**
     * Gets the current XML mapper.
     *
     * @return the mapper
     */
    @Override
    public XmlMapper xmlMapper() {
        synchronized (lock) {
            return xml;
        }
    }

    /**
     * Builds a new XML Document from the given (xml) string.
     * By default this method uses UTF-8. If your document does not use UTF-8,
     * use {@link #fromInputStream(java.io.InputStream, java.nio.charset.Charset)} that let you set the encoding.
     *
     * @param xml the xml to parse, must not be {@literal null}
     * @return the document
     * @throws java.io.IOException if the given string is not a valid XML document
     */
    @Override
    public Document fromString(String xml) throws IOException {
        ByteArrayInputStream stream = null;
        try {
            stream = new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8));
            return fromInputStream(
                    stream,
                    Charsets.UTF_8
            );
        } catch (UnsupportedEncodingException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

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
    @Override
    public Document fromInputStream(InputStream stream, Charset encoding) throws IOException {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputSource is = new InputSource(stream);
            if (encoding == null) {
                is.setEncoding(Charsets.UTF_8.name());
            } else {
                is.setEncoding(encoding.name());
            }

            return builder.parse(is); //NOSONAR The used factory is not exposed to XXE.

        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Cannot parse the given XML document", e);
        }
    }

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given XML document.
     *
     * @param document the XML document
     * @param clazz    the class of the instance to construct
     * @return an instance of the class.
     */
    @Override
    public <A> A fromXML(Document document, Class<A> clazz) {
        return fromXML(stringify(document), clazz);
    }

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given XML string.
     *
     * @param xml   the XML string
     * @param clazz the class of the instance to construct
     * @return an instance of the class.
     */
    @Override
    public <A> A fromXML(String xml, Class<A> clazz) {
        try {
            return xmlMapper().readValue(xml, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the string form of the given XML document.
     *
     * @param document the XML document, must not be {@literal null}
     * @return the String form of the object
     */
    @Override
    public String stringify(Document document) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new document.
     *
     * @return the document
     */
    public Document newDocument() {
        try {
            return factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Binds a module.
     *
     * @param module the module
     */
    @Bind(optional = true, aggregate = true)
    public synchronized void bindModule(Module module) {
        register(module);
    }

    /**
     * Unbinds a module.
     *
     * @param module the module
     */
    @Unbind
    public synchronized void unbindModule(Module module) {
        unregister(module);
    }

}
