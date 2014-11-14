# Wisdom Filters

This extension to Wisdom Framework contains a set of `filters` easing the development of:

* Transparent Proxies
* Balancers with or with reverse routing (in progress)


## Installing

Installing this extension is quite easy, just add the following Maven dependency to your project:

````
<dependency>
    <groupId>org.wisdom-framework</groupId>
    <artifactId>wisdom-filters</artifactId>
    <!-- update version to latest release -->
    <version>0.7-SNAPSHOT</version>
</dependency>
````

## Transparent Proxy

The `org.wisdom.framework.filters.ProxyFilter` class lets you create a transparent proxy, i.e a transparent
redirection to another URL. You need to extend the `ProxyFilter` class to create a transparent proxy:

````
@Service
public class HttpBinFilter extends ProxyFilter implements Filter {

    @Override
    protected String getProxyTo() {
        return "http://httpbin.org/get";
    }

    @Override
    protected String getPrefix() {
        return "/proxy/get";
    }
}
````

The `getProxyTo` and `getPrefix` configure the proxy:

* `getProxyTo` specifies the destination URL.
* `getPrefix` specifies the root url that are transferred to the destination

In the previous example all requests targeting `/proxy/get` are transferred to `http://httpbin.org/get`.

The `ProxyFilter` class implements the `uri` and `priority` (from the `Filter` interface) methods as follows:

* the uri (regex) is computed from the given the set prefix.
* the priority is set to 1000

The `ProxyFilter` class lets you configure several other aspects of the proxy by overriding methods (see below to
configure the proxy from the application configuration):

* `newHttpClient`: by overriding this method you can provide a custom (Apache) HTTP Client instance with a custom
configuration
* `followRedirect`: specifies whether or not `redirections` need to be followed
* `createLogger` : sets the SLF4J Logger used by the proxy
* `updateHeaders`: lets you update the headers sent to the destination. By default it uses all headers from the
incoming request (minus the exceptions to be conformed to http://tools.ietf.org/html/rfc7230#section-6.1.).
* `onResult`: let you modify the `Result` returned by the destination before being sent to the client.
* `rewriteURI`: customize the destination URI. By default the destination is computed from the `getProxyTo` and
`getPrefix` methods. It subtracts the prefix from the incoming request path, and append the result to the _proxyTo_
URI. The query is also appended as given. The `rewriteURI` method lets you override this logic by your own.
* `getHost`: configure the `host` header to be sent in the request to the destination. Some host check that the host
 header matches their own URL.
* `getVia`: configure the `via` header

Most part of the configuration can be retrieved from the `ApplicationConfiguration` as follows:

````
@Service
public class XMLFilterUsingConfiguration extends ProxyFilter implements Filter {

    public XMLFilterUsingConfiguration(@Requires ApplicationConfiguration c) {
       super(c.getConfiguration("myfilter"));
    }
}
````

Then, the application configuration should contain:

````
# My Filter Configuration
# ~~~~~~~~~~~~~~~~~~~~~~~
myfilter.prefix = /proxy/xml2
myfilter.proxyTo = http://httpbin.org/xml
````

The `prefix`, `proxyTo`, `host` header and `via` header can be configured using this feature.

**Note**
Because the transparent proxy is emitting another request, which can take time to be processed, the filter returns an
 `AsyncResult` and not a (synchronous) `Result`.