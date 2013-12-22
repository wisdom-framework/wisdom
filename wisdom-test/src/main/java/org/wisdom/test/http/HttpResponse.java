package org.wisdom.test.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private boolean isGzipped() {
        Set<Map.Entry<String, String>> headers = this.headers.entrySet();
        for (Map.Entry<String, String> header : headers) {
            if (header.getKey().equalsIgnoreCase("content-encoding")) {
                if (header.getValue() != null && header.getValue().equalsIgnoreCase("gzip")) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public HttpResponse(org.apache.http.HttpResponse response, Class<T> responseClass) {
        HttpEntity responseEntity = response.getEntity();

        Header[] allHeaders = response.getAllHeaders();
        this.headers = new HashMap<String, String>();
        for (Header header : allHeaders) {
            headers.put(header.getName().toLowerCase(), header.getValue());
        }
        this.code = response.getStatusLine().getStatusCode();

        if (responseEntity != null) {
            try {
                byte[] rawBody;
                try {
                    InputStream responseInputStream = responseEntity.getContent();
                    if (isGzipped()) {
                        responseInputStream = new GZIPInputStream(responseEntity.getContent());
                    }
                    rawBody = getBytes(responseInputStream);
                } catch (IOException e2) {
                    throw new RuntimeException(e2);
                }
                this.rawBody = new ByteArrayInputStream(rawBody);

                if (JsonNode.class.equals(responseClass)) {
                    String jsonString = new String(rawBody).trim();
                    this.body = (T) new ObjectMapper().readValue(jsonString, JsonNode.class);
                } else if (Document.class.equals(responseClass)) {
                    String raw = new String(rawBody).trim();
                    this.body = (T) Jsoup.parse(raw);
                } else if (String.class.equals(responseClass)) {
                    this.body = (T) new String(rawBody);
                } else if (InputStream.class.equals(responseClass)) {
                    this.body = (T) this.rawBody;
                } else {
                    throw new Exception("Unknown result type. Only String, JsonNode, " +
                            "Document and InputStream are supported.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
