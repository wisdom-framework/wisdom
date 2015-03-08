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

import org.fluentlenium.adapter.FluentTest;
import org.fluentlenium.core.Fluent;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.maven.utils.ChameleonInstanceHolder;
import org.wisdom.test.WisdomBlackBoxRunner;

/**
 * When testing a Wisdom Application in 'black box' mode (i.e. by emitting HTTP requests),
 * this class provides a couple of useful method easing test implementation.
 */
@RunWith(WisdomBlackBoxRunner.class)
public class WisdomFluentLeniumTest extends FluentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomFluentLeniumTest.class);

    private String hostname;
    private int httpPort;

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

    @Override
    public WebDriver getDefaultDriver() {
        String browser = System.getProperty("fluentlenium.browser");
        LOGGER.debug("Selecting Selenium Browser using " + browser);
        if (browser == null) {
            LOGGER.debug("Using default HTML Unit Driver");
            return new HtmlUnitDriver();
        }

        if ("chrome".equalsIgnoreCase(browser)) {
            LOGGER.debug("Using Chrome");
            return new ChromeDriver();
        }

        if ("firefox".equalsIgnoreCase(browser)) {
            LOGGER.debug("Using Firefox");
            return new FirefoxDriver();
        }

        if ("ie".equalsIgnoreCase(browser) || "internetexplorer".equalsIgnoreCase(browser)) {
            LOGGER.debug("Using Internet Explorer");
            return new InternetExplorerDriver();
        }

        if ("safari".equalsIgnoreCase(browser)) {
            LOGGER.debug("Using Safari");
            return new SafariDriver();
        }

        throw new IllegalArgumentException("Unknown browser : " + browser);
    }

    @Override
    public Fluent goTo(String url) {
        return super.goTo(getHttpURl(url));
    }
}
