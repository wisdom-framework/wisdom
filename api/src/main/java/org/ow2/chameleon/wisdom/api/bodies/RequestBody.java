package org.ow2.chameleon.wisdom.api.bodies;

import com.fasterxml.jackson.databind.JsonNode;
import org.ow2.chameleon.wisdom.api.http.MultipartFormData;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * The default implementation of request body.
 */
public class RequestBody {

    public boolean isMaxSizeExceeded() {
        return false;
    }

    /**
     * The request content parsed as multipart form data.
     */
    public MultipartFormData asMultipartFormData() {
        return null;
    }

    /**
     * The request content parsed as URL form-encoded.
     */
    public Map<String,String[]> asFormUrlEncoded() {
        return null;
    }

    /**
     * The request content as Array bytes.
     */
    public RawBuffer asRaw() {
        return null;
    }

    /**
     * The request content as text.
     */
    public String asText() {
        return null;
    }

    /**
     * The request content as XML.
     */
    public Document asXml() {
        return null;
    }

    /**
     * The request content as Json.
     */
    public JsonNode asJson() {
        return null;
    }

    /**
     * Cast this RequestBody as T if possible.
     */
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> tType) {
        if(this.getClass().isAssignableFrom(tType)) {
            return (T)this;
        } else {
            return null;
        }
    }

}
