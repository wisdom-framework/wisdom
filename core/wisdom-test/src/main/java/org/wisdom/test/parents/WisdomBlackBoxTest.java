/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.test.parents;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Status;
import org.wisdom.maven.utils.ChameleonInstanceHolder;
import org.wisdom.test.WisdomBlackBoxRunner;
import org.wisdom.test.http.GetRequest;
import org.wisdom.test.http.HttpRequestWithBody;
import org.wisdom.test.internals.ChameleonExecutor;
import org.wisdom.test.internals.ProbeBundleMaker;

/**
 * When testing a Wisdom Application in 'black box' mode (i.e. by emitting HTTP requests),
 * this class provides a couple of useful method easing test implementation.
 */
@RunWith(WisdomBlackBoxRunner.class)
public class WisdomBlackBoxTest implements HeaderNames, Status {

    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomBlackBoxTest.class);


    private String hostname;
    private int httpPort;
    private int httpsPort;


    public static void installTestBundle() throws BundleException {
        BundleContext context = ChameleonInstanceHolder.get().context();
        // Check whether the test bundle is already installed
        Bundle probe = null;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().equalsIgnoreCase(ProbeBundleMaker.BUNDLE_NAME)) {
                probe = bundle;
            }
        }

        if (probe != null) {
            LOGGER.info("Probe / Test bundle already deployed");
        } else {
            probe = ChameleonExecutor.deployProbe();
            LOGGER.info("Probe / Test bundle deployed ({})", probe.getBundleId());
            ChameleonInstanceHolder.get().waitForStability();
        }
    }

    public static void removeTestBundle() throws BundleException {
        BundleContext context = ChameleonInstanceHolder.get().context();
        // Check whether the test bundle is already installed
        Bundle probe = null;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().equalsIgnoreCase(ProbeBundleMaker.BUNDLE_NAME)) {
                probe = bundle;
            }
        }

        if (probe != null) {
            probe.uninstall();
            LOGGER.info("Probe / Test bundle uninstall ({})", probe.getBundleId());
            ChameleonInstanceHolder.get().waitForStability();
        }
    }

    /**
     * Methods call by the test framework to discover the server name and port.
     *
     * @throws Exception if the service is not running.
     */
    @Before
    public void retrieveServerMetadata() throws Exception {

        if (hostname != null) {
            return;
        }

        hostname = ChameleonInstanceHolder.getHostName();
        httpPort = ChameleonInstanceHolder.getHttpPort();
        httpsPort = ChameleonInstanceHolder.getHttpsPort();
    }

    /**
     * Computes the full url from the given path. If the given path already starts by "http",
     * the path is returned as given.
     *
     * @param path the path
     * @return the HTTP url built as follows: http://server_name:server_port/path
     */
    public String getHttpURl(String path) {
        String localUrl = path;
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

    /**
     * Computes the full url from the given path. If the given path already starts by "https",
     * the path is returned as given.
     *
     * @param path the path
     * @return the HTTPS url built as follows: https://server_name:server_port/path
     */
    public String getHttpsURl(String path) {
        String localUrl = path;
        if (localUrl.startsWith("https")) {
            return localUrl;
        } else {
            // Prepend with hostname and port
            if (!localUrl.startsWith("/")) {
                localUrl = '/' + localUrl;
            }
            return "https://" + hostname + ":" + httpsPort + localUrl;
        }
    }

    /**
     * Creates a request using the 'GET' verb.
     *
     * @param url the path
     * @return the request
     */
    public GetRequest get(String url) {
        return new GetRequest(HttpMethod.GET, getHttpURl(url));
    }

    /**
     * Creates a 'POST' request.
     *
     * @param url the path
     * @return the request
     */
    public HttpRequestWithBody post(String url) {
        return new HttpRequestWithBody(HttpMethod.POST, getHttpURl(url));
    }

    /**
     * Creates a 'DELETE' request.
     *
     * @param url the path
     * @return the request
     */
    public HttpRequestWithBody delete(String url) {
        return new HttpRequestWithBody(HttpMethod.DELETE, getHttpURl(url));
    }

    /**
     * Creates a 'PUT' request.
     *
     * @param url the path
     * @return the request
     */
    public HttpRequestWithBody put(String url) {
        return new HttpRequestWithBody(HttpMethod.PUT, getHttpURl(url));
    }

}
