package org.wisdom.test.parents;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceReference;
import org.wisdom.api.engine.WisdomEngine;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Status;
import org.wisdom.test.WisdomBlackBoxRunner;
import org.wisdom.test.http.GetRequest;
import org.wisdom.test.http.HttpRequestWithBody;
import org.wisdom.test.internals.ChameleonExecutor;

/**
 *
 */
@RunWith(WisdomBlackBoxRunner.class)
public class WisdomBlackBoxTest implements HeaderNames, Status{


    private String hostname;
    private int httpPort;

    @Before
    public void retrieveServerMetadata() throws Exception {
        if (hostname != null) {
            return;
        }

        ServiceReference<?> reference = ChameleonExecutor.instance(null).context().getServiceReference(WisdomEngine.class
                .getName());
        Object engine = ChameleonExecutor.instance(null).context().getService(reference);
        hostname = (String) engine.getClass().getMethod("hostname").invoke(engine);
        httpPort = (int) engine.getClass().getMethod("httpPort").invoke(engine);
    }

    public String getHttpURl(String url) {
        String localUrl = url;
        if (localUrl.startsWith("http")) {
            return localUrl;
        } else {
            // Prepend with hostname and port
            if (!localUrl.startsWith("/")) {
                localUrl = '/' + localUrl;
            }
            return "http://" + hostname + ":" + httpPort + localUrl;
        }
    }

    public GetRequest get(String url) {
        return new GetRequest(HttpMethod.GET, getHttpURl(url));
    }

    public HttpRequestWithBody post(String url) {
        return new HttpRequestWithBody(HttpMethod.POST, getHttpURl(url));
    }

    public HttpRequestWithBody delete(String url) {
        return new HttpRequestWithBody(HttpMethod.DELETE, getHttpURl(url));
    }

    public HttpRequestWithBody put(String url) {
        return new HttpRequestWithBody(HttpMethod.PUT, getHttpURl(url));
    }

}
