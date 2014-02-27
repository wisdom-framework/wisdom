package org.wisdom.test.parents;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Response;
import org.wisdom.api.router.Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A fake implementation of the context.
 */
public class FakeContext implements Context {

    private static AtomicLong counter = new AtomicLong();

    private Map<String, List<String>> attributes = Maps.newHashMap();

    private Map<String, List<String>> parameters = Maps.newHashMap();
    private SessionCookie session = new FakeSessionCookie();
    private FlashCookie flash = new FakeFlashCookie();
    private Object body;

    private Map<String, List<String>> headers = new HashMap<>();


    @Override
    public Long id() {
        return counter.getAndIncrement();
    }

    @Override
    public Request request() {
        return null;  
    }

    @Override
    public Response response() {
        return null;  
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public FlashCookie flash() {
        return flash;
    }

    @Override
    public SessionCookie session() {
        return session;
    }

    @Override
    public Cookie cookie(String cookieName) {
        return null;  
    }

    @Override
    public boolean hasCookie(String cookieName) {
        return false;  
    }

    @Override
    public Cookies cookies() {
        return null; 
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String parameter(String name) {
        if (parameters.get(name) != null) {
            return parameters.get(name).get(0);
        }
        return null;
    }

    @Override
    public List<String> parameterMultipleValues(String name) {
        return parameters.get(name);
    }

    @Override
    public String parameter(String name, String defaultValue) {
        String v = parameter(name);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    @Override
    public Integer parameterAsInteger(String name) {
        String v = parameter(name);
        if (v != null) {
            return Integer.parseInt(v);
        }
        return null;
    }

    @Override
    public Integer parameterAsInteger(String name, Integer defaultValue) {
        String v = parameter(name);
        if (v != null) {
            return Integer.parseInt(v);
        }
        return defaultValue;
    }

    @Override
    public Boolean parameterAsBoolean(String name) {
        String v = parameter(name);
        if (v != null) {
            return Boolean.parseBoolean(v);
        }
        return null;
    }

    @Override
    public Boolean parameterAsBoolean(String name, boolean defaultValue) {
        String v = parameter(name);
        if (v != null) {
            return Boolean.parseBoolean(v);
        }
        return defaultValue;
    }

    @Override
    public String parameterFromPath(String name) {
        return parameter(name);
    }

    @Override
    public String parameterFromPathEncoded(String name) {
        return parameter(name);
    }

    @Override
    public Integer parameterFromPathAsInteger(String key) {
        return parameterAsInteger(key);
    }

    @Override
    public Map<String, List<String>> parameters() {
        return parameters;
    }

    @Override
    public String header(String name) {
        List<String> v = headers.get(name);
        if (v != null  && ! v.isEmpty()) {
            return v.get(0);
        }
        return null;
    }

    @Override
    public List<String> headers(String name) {
        return new ArrayList<>(headers.keySet());
    }

    @Override
    public Map<String, List<String>> headers() {
        return headers;
    }

    @Override
    public String cookieValue(String name) {
        return null;  
    }

    @Override
    public <T> T body(Class<T> classOfT) {
        return (T) body;
    }

    @Override
    public String body() {
        if (body != null) {
            return body.toString();
        }
        return null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;  
    }

    @Override
    public Route getRoute() {
        return null;  
    }

    @Override
    public void setRoute(Route route) {
        //Fake
    }

    @Override
    public boolean isMultipart() {
        return false;  
    }

    @Override
    public Collection<? extends FileItem> getFiles() {
        return new ArrayList<>();  
    }

    @Override
    public FileItem getFile(String name) {
        return null;  
    }

    @Override
    public Map<String, List<String>> attributes() {
        return attributes;
    }

    public void setAttribute(String name, String value) {
        List<String> values = attributes.get(name);
        if (values == null) {
            values = new ArrayList<>();
            attributes.put(name, values);
        }
        values.add(name);
    }

    public void setParameter(String name, String value) {
        List<String> list = Lists.newArrayList();
        list.add(value);
        setParameter(name, list);
    }

    public void setParameter(String name, List<String> values) {
         parameters.put(name, values);
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public void setHeader(String name, String value) {
        List<String> list = headers.get(name);
        if (list == null) {
            list = new ArrayList<>();
            headers.put(name, list);
        }
        list.add(value);
    }

    public void setHeader(String name, String... values) {
        List<String> list = new ArrayList<>(Arrays.asList(values));
        headers.put(name, list);
    }
}
