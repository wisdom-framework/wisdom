package org.wisdom.framework.filters.test;

import org.apache.felix.ipojo.annotations.Validate;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.interception.Filter;
import org.wisdom.framework.filters.ProxyFilter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.LogManager;
import java.util.regex.Pattern;

@Service
public class RedirectFilter extends ProxyFilter implements Filter {

    @Validate
    public void init() throws IOException {
        LogManager.getLogManager().readConfiguration(new FileInputStream("/Users/clement/Projects/wisdom/wisdom-filters/src/logging.properties"));
        System.out.println("Configured");
    }

    @Override
    protected String getProxyTo() {
        return "http://httpbin.org/redirect-to?url=http://perdu.com";
    }

    @Override
    protected boolean followRedirect(String method) {
        return true;
    }

    /**
     * Gets the Regex Pattern used to determine whether the route is handled by the filter or not.
     * Notice that the router are caching these patterns and so cannot changed.
     */
    @Override
    public Pattern uri() {
        return Pattern.compile("/proxy/redirect");
    }

    /**
     * Gets the filter priority, determining the position of the filter in the filter chain. Filter with a high
     * priority are called first. Notice that the router are caching these priorities and so cannot changed.
     * <p>
     * It is heavily recommended to allow configuring the priority from the Application Configuration.
     *
     * @return the priority
     */
    @Override
    public int priority() {
        return 1000;
    }

    @Override
    protected String getPrefix() {
        return "/proxy/redirect";
    }
}
