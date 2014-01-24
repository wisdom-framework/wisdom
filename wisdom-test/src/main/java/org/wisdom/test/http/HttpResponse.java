package org.wisdom.test.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class HttpResponse<T> {

    private int code;
    private Map<String, String> headers;
    private InputStream rawBody;
    private T body;
    
    @SuppressWarnings("unchecked")
    public HttpResponse(org.apache.http.HttpResponse response, Class<T> responseClass) {
        HttpEntity responseEntity = response.getEntity();

        Header[] allHeaders = response.getAllHeaders();
        this.headers = new HashMap<>();
        for (Header header : allHeaders) {
            headers.put(header.getName().toLowerCase(), header.getValue());
        }
        this.code = response.getStatusLine().getStatusCode();

        if (responseEntity != null) {
            try {
                byte[] raw;
                InputStream responseInputStream = responseEntity.getContent();
                if (isGzipped()) {
                    responseInputStream = new GZIPInputStream(responseEntity.getContent());
                }
                raw = getBytes(responseInputStream);
                this.rawBody = new ByteArrayInputStream(raw);

                if (JsonNode.class.equals(responseClass)) {
                    String jsonString = new String(raw, Charsets.UTF_8).trim();
                    this.body = (T) new ObjectMapper().readValue(jsonString, JsonNode.class);
                } else if (Document.class.equals(responseClass)) {
                    String r = new String(raw, Charsets.UTF_8).trim();
                    this.body = (T) Jsoup.parse(r);
                } else if (String.class.equals(responseClass)) {
                    this.body = (T) new String(raw);
                } else if (InputStream.class.equals(responseClass)) {
                    this.body = (T) this.rawBody;
                } else {
                    throw new IllegalArgumentException("Unknown result type. Only String, JsonNode, " +
                            "Document and InputStream are supported.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private boolean isGzipped() {
        Set<Map.Entry<String, String>> heads = headers.entrySet();
        for (Map.Entry<String, String> header : heads) {
            if ("content-encoding".equalsIgnoreCase(header.getKey()) && "gzip".equalsIgnoreCase(header.getValue())) {
                return true;
            }
        }
        return false;
    }

    private static byte[] getBytes(InputStream is) throws IOException {
        return IOUtils.toByteArray(is);
    }

    public int code() {
        return code;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public InputStream raw() {
        return rawBody;
    }

    public T body() {
        return body;
    }
}
