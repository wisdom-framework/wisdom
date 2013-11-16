package org.ow2.chameleon.wisdom.content.bodyparsers;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.content.BodyParser;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;

@Component
@Provides
@Instantiate
public class BodyParserForm implements BodyParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public <T> T invoke(Context context, Class<T> classOfT) {
        T t = null;
        try {
            t = classOfT.newInstance();
        } catch (Exception e) {
            logger.error("can't newInstance class " + classOfT.getName(), e);
            return null;
        }
        for (Entry<String, List<String>> ent : context.parameters().entrySet()) {
            try {
                Field field = classOfT.getDeclaredField(ent.getKey());
                field.setAccessible(true);
                field.set(t, ent.getValue().get(0));
            } catch (Exception e) {
                logger.warn(
                        "Error parsing incoming form data for key " + ent.getKey()
                                + " and value " + ent.getValue(), e);
            }
        }
        if (context.attributes() != null) {
            for (Entry<String, String> ent : context.attributes().entrySet()) {
                try {
                    Field field = classOfT.getDeclaredField(ent.getKey());
                    field.setAccessible(true);
                    field.set(t, ent.getValue());
                } catch (NoSuchFieldException e) {
                    logger.warn("No member in {} to be bound with attribute {}={}", classOfT.getName(), ent.getKey(),
                            ent.getValue());
                } catch (Exception e) {
                    logger.warn(
                            "Error parsing incoming form data for key " + ent.getKey()
                                    + " and value " + ent.getValue(), e);
                }
            }
        }
        return t;
    }

    @Override
    public <T> T invoke(byte[] bytes, Class<T> classOfT) {
        throw new UnsupportedOperationException("Cannot bind a raw byte[] to a form object");
    }

    public String getContentType() {
        return MimeTypes.FORM;
    }
}
