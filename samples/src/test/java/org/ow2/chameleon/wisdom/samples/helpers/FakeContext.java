package org.ow2.chameleon.wisdom.samples.helpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.ow2.chameleon.wisdom.api.cookies.Cookie;
import org.ow2.chameleon.wisdom.api.cookies.Cookies;
import org.ow2.chameleon.wisdom.api.cookies.FlashCookie;
import org.ow2.chameleon.wisdom.api.cookies.SessionCookie;
import org.ow2.chameleon.wisdom.api.http.*;
import org.ow2.chameleon.wisdom.api.route.Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A fake implementation of the context.
 */
public class FakeContext implements Context {

    private static AtomicLong counter = new AtomicLong();

    private Map<String, String> attributes = Maps.newHashMap();

    private Map<String, List<String>> parameters = Maps.newHashMap();


    @Override
    public Long id() {
        return counter.getAndIncrement();
    }

    @Override
    public Request request() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response response() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public FlashCookie flash() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SessionCookie session() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Cookie cookie(String cookieName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasCookie(String cookieName) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Cookies cookies() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> headers(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, List<String>> headers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String cookieValue(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T body(Class<T> classOfT) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Route getRoute() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRoute(Route route) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isMultipart() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<? extends FileItem> getFiles() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FileItem getFile(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> attributes() {
        return attributes;
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public void setParameter(String name, String value) {
        List<String> list = Lists.newArrayList();
        list.add(value);
        setParameter(name, list);
    }

    public void setParameter(String name, List<String> values) {
         parameters.put(name, values);
    }
}
