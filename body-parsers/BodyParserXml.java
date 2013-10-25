package org.ow2.chameleon.wisdom.bodyparsers.parsers;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.bodyparser.BodyParser;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@Provides
@Instantiate
public class BodyParserXml implements BodyParser {

    private final XmlMapper xmlMapper;
    private final Logger logger = LoggerFactory.getLogger(BodyParserXml.class);


    public BodyParserXml(Logger logger) {
        JacksonXmlModule module = new JacksonXmlModule();
        // Check out: https://github.com/FasterXML/jackson-dataformat-xml
        // setDefaultUseWrapper produces more similar output to
        // the Json output. You can change that with annotations in your
        // models.
        module.setDefaultUseWrapper(false);
        this.xmlMapper = new XmlMapper(module);

    }

    public <T> T invoke(Context context, Class<T> classOfT) {
        T t = null;
        try {
            t = xmlMapper.readValue(context.getReader(), classOfT);
        } catch (IOException e) {
            logger.error("Error parsing incoming Xml", e);
        }
        return t;
    }

    public String getContentType() {
        return MimeTypes.XML;
    }

}
