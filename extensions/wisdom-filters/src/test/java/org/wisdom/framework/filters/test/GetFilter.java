package org.wisdom.framework.filters.test;

import org.wisdom.api.annotations.Service;
import org.wisdom.api.interception.Filter;
import org.wisdom.framework.filters.ProxyFilter;

import java.util.regex.Pattern;

@Service
public class GetFilter extends ProxyFilter implements Filter {

    @Override
    protected String getProxyTo() {
        return "http://httpbin.org/get";
    }

    /**
     * Gets the Regex Pattern used to determine whether the route is handled by the filter or not.
     * Notice that the router are caching these patterns and so cannot changed.
     */
    @Override
    public Pattern uri() {
        return Pattern.compile("/proxy/get");
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
        return "/proxy/get";
    }
}
