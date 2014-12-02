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

##  CORS support

Cross-origin resource sharing (CORS) is a mechanism that allows many resources (e.g., fonts, JavaScript, etc.) on a web
page to be requested from another domain outside the domain from which the resource originated.In particular,
JavaScript's AJAX calls can use the XMLHttpRequest mechanism. Such "cross-domain" requests would otherwise be forbidden
by web browsers, per the same-origin security policy. CORS defines a way in which the browser and the server can
interact to determine whether or not to allow the cross-origin request. It is more useful than only allowing same-origin
 requests, but it is more secure than simply allowing all such cross-origin requests.

The CORS filter lets you support CORS following the W3C recommendation (http://www.w3.org/TR/cors/). CORS can be
enabled using the `application.conf` file:

```
# CORS Configuration
# ~~~~~~~~~~~~~~~~~~
cors.enabled = true
cors.allow-origin = *
cors.allow-headers = X-Custom-Header
cors.allow-credentials = true
cors.max-age = 86400
```

By default CORS are disabled.

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

## Redirect Filter

The `org.wisdom.framework.filters.RedirectFilter` filter computes an URL and build a `SEE_OTHER` response, instructing the client to be redirected to the computed URL. You need to extend the `RedirectFilter` class to create a redirection
filter:

````
@Service
public class MyRedirectionFilter extends RedirectFilter implements Filter {

    @Override
    protected String getRedirectTo() {
        return "http://my-target.com";
    }

    @Override
    protected String getPrefix() {
        return "/redirect";
    }
}
````

The `getRedirectTo` and `getPrefix` methods configure the proxy:

* `getRedirectTo` specifies the destination URL.
* `getPrefix` specifies the root url that are transferred to the destination

In the previous example all requests targeting `/redirect` are redirected to `http://my-target.com`.

The `RedirectFilter` class implements the `uri` and `priority` (from the `Filter` interface) methods as follows:

* the uri (regex) is computed from the given the set prefix.
* the priority is set to 1000

The `RedirectFilter` class lets you configure several other aspects by overriding methods (see below to
configure the filter from the application configuration):

* `createLogger` : sets the SLF4J Logger used by the proxy
* `rewriteURI`: customize the destination URI. By default the destination is computed from the `getRedirectTo` and
`getPrefix` methods. It subtracts the prefix from the incoming request path, and append the result to the _redirectTo_
URI. The query is also appended as given. The `rewriteURI` method lets you override this logic by your own.

Most part of the configuration can be retrieved from the `ApplicationConfiguration` as follows:

````
@Service
public class MyRedirectFilter extends RedirecttFilter implements Filter {

    public MyRedirectFilter(@Requires ApplicationConfiguration c) {
       super(c.getConfiguration("my-redirect"));
    }
}
````

Then, the application configuration should contain:

````
my-redirect.prefix = /proxy/xml2
my-redirect.redirectTo = http://httpbin.org/xml
````

## Balancer Filter

The `org.wisdom.framework.filters.BalancerFilter` filter lets you implement a load balancer strategy. It manages a
set of `org.wisdom.framework.filters.BalancerMember` and delegate the request to a chosen member. It supports sticky
session and reverse routing. In addition, it supports the addition and removal of member at runtime. This impacts the
 stick session strategy as the member may have left. In that case, it falls back to another member.

To implement a load balancer you need:
 * a balancer implementation - it's a filter selecting a member and delegating the request to it
 * members describing the destination hosts.

You need to extend the `BalancerFilter` class to create a load balancer:

```
@Service
public class MyBalancer extends BalancerFilter implements Filter {

    @Override
    public String getName() {
        return "balancer";
    }

    @Override
    protected String getPrefix() {
        return "/balancer";
    }

    @Bind(aggregate = true, optional = true)
    public void bindMember(BalancerMember member) {
        addMember(member);
    }

    @Unbind
    public void unbindMember(BalancerMember member) {
        removeMember(member);
    }
}
```

First, this `BalanceFilter` collects the `BalancerMember` instances from the service registry, and follow their arrival
 and departure (thanks to the `@Bind` and `@Unbind` callbacks). It defines its name and the prefix. The name is
particularly important, as `BalancerMember` must refer to it (see below). A `BalancerMember` is describing to which
host the request is delegated. In the previous implementation (`MyBalancer`), they are collected from the service
registry.

To provide a member, you have to extend the `org.wisdom.framework.filters.DefaultBalancerMember` class:

```
@Service
public class BalancerMember1 extends DefaultBalancerMember implements BalancerMember {
    public BalancerMember1() {
        super("member-1", "http://perdu.com", "balancer");
    }
}
```

You need to specify the name of the member, the destination and the balancer name. You can also create a member
collecting its configuration from the application configuration:

```
@Service
public class BalancerMember2 extends DefaultBalancerMember implements BalancerMember {
   public BalancerMember2(@Requires ApplicationConfiguration configuration) {
           super(configuration.getConfiguration("member"));
       }
}
```

The configuration would be like the following:

```
member.name = member-2
member.balancerName = balancer
member.proxyTo = http://perdus.com
```

The balancer strategy implements a round robin by default. You can enable or disable the sticky session support by
overriding the `getStickySession` method (disabled by default). In addition, you can enable or disable the reverse
routing computation by overriding the `getProxyPassReverse` method (disabled by default). The round robin strategy is
 implemented in the `selectBalancerMember` method that you can override to adapt the behavior.

Most of the balancer configuration can be provided from the `application.conf` file. In that case, your extension of
`BalancerFilter` must provide the `Configuration` object to its super constructor:

```
public MyBalancer(@Requires ApplicationConfiguration configuration) {
        super(configuration.getConfiguration("balancer"));
}
```

In that case the configuration looks like:
```
# Name is mandatory, prefix is highly recommended (if not set, it will intercept /)
balancer.name=my-balancer
balancer.prefix=/prefix
# sticky session and proxy pass reverse are optional (false by default)
balancer.stickySession=true
balancer.proxyPassReverse=true
```