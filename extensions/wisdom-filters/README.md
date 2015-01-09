# Wisdom Filters

This extension to Wisdom Framework contains a set of `filters` easing the development of:

* Transparent Proxies
* Balancers with or with reverse routing (in progress)
* CORS handling
* CSRF protection


## Installing

Installing this extension is quite easy, just add the following Maven dependency to your project:

````
<dependency>
    <groupId>org.wisdom-framework</groupId>
    <artifactId>wisdom-filters</artifactId>
    <!-- update version to latest release -->
    <version>0.7.0</version>
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

## CSRF Protection

Cross-Site Request Forgery (CSRF) is a type of attack that occurs when a malicious Web site, email, blog, instant message, or program causes a user's Web browser to perform an unwanted action on a trusted site for which the user is currently authenticated. This module provides two interceptors to protect your application against CSRF attack.

### Configuring the CSRF support

First, you need to configure the CSRF support in the `application.conf` file:

````
# CSRF Configuration
# ~~~~~~~~~~~~~~~~~~
csrf {
  token {
    name = "csrf_token"
    sign = true # Optional, true by default
  }
  cookie { # Optional using session by default
    name = "csrf_cookie"
    secure = false
  }
}
````

`csrf.token.name` let you set the name of the CSRF token. This name must match the form field or query parameter
giving the token back. By default `csrfToken` is used.

`csrf.token.sign` let you decide whether or not the token need to be signed. Token are signed by default, enhancing
security, but requiring signing and extraction process.

By default, the _reference_ token is sent in the session, but you can use a cookie instead. To enable this feature
add the `csrf.cookie.name` property indicating the cookie name. You can also configure whether or not the cookie is
secure with the `secure` property (true by default), the `path` (`/` by default), and the domain:

````
cookie {
    name = "cookie_name"
    secure = true
    domain = "localhost"
    path = "/"
}
````

### Generating a token

Once configured, you can generate a token using the `org.wisdom.framework.csrf.api.AddCSRFToken` on your action or on
 the controller. This annotation instruct Wisdom to generate a new token and to inject it in the response.

````
@Route(method = HttpMethod.GET, uri = "/csrf")
@AddCSRFToken
public Result getPage(@HttpParameter(AddCSRFToken.CSRF_TOKEN) String token) {
    return ok(render(template, "token", token));
}
````

The generated token can be retrieved using `@HttpParameter(AddCSRFToken.CSRF_TOKEN) String token`, and then inject
into a template:

````
<input name="csrf_token" id="csrf_token" type="hidden" th:value="${token}"/>
````

Notice that the field name must match the `csrf.token.name` property.

### Protecting your route against CSRF attack

Now that your token is injected, you can protect your other action method using the
`org.wisdom.framework.csrf.api.CSRF` annotation. When used, Wisodm checks that the request contains a valid CSRF
token before invoking the action. If the token is missing or invalid a `FORBIDDEN` result is returned:

````
@Route(method = HttpMethod.POST, uri = "/csrf")
@CSRF
public Result submitted(@FormParameter("key") String key) {
    return ok(key);
}
````

### Customizing the response on invalid requests

You may want to customize the result when the token is missing or invalid. By default, it returns a `FORBIDDEN`
result. This can be changed by exposing a service:

````
@Service
public class MyCSRFErrorHandler implements CSRFErrorHandler {

    @Override
    public Result onError(Context context, String reason) {
        return Results.forbidden("you shall not pass - Gandalf");
    }
}
````

### Passing the token using a HTTP header

As explained above, the request needs to convey the token either using a query parameter or a form field. Both need
to have the name specified in the configuration. There is another way using the `X-XSRF-TOKEN` HTTP Header.

### By passing the CSRF check

The `X-XSRF-TOKEN` HTTP header can also be used to voluntary bypass the CSRF check. By passing the `no-check` value
to this header, Wisdom accepts the request without checking. This is possible because it's not possible to inject
arbitrary header values with a CSRF attack.

Also, AJAX requests are not checked.

### Using the `csrf:token` element in template

If you are writing a Thymeleaf template, you can inject a CSRF token in your form using:

````
<form action="/csrf/check" method="post">
    <csrf:token/>
    <input name="key" id="key" type="text"/>
    <button type="submit">Submit</button>
</form>
````

So, you don't have to inject the token explicitly. However, you still need to use the `@AddCSRFToken` annotation to
generate the token:

````
@Route(method = HttpMethod.GET, uri = "/csrf/dialect")
@AddCSRFToken
public Result getPageUsingDialect() {
    return ok(render(templateWithDialect));
}
````