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
package org.wisdom.resources;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.crypto.Hash;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wisdom.test.parents.Action.action;

/**
 * Check the Web Jar Controller
 */
public class WebJarControllerTest {

    private File root = new File("target/wisdom-test");
    private File webjars = new File(root, "assets/libs");

    @Before
    public void setUp() {
        root.mkdirs();
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(root);
    }

    @Test
    public void testOnMissingFolder() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        Crypto crypto = mock(Crypto.class);
        root = new File("target/wisdom-test");
        when(configuration.getBaseDir()).thenReturn(root);

        WebJarController controller = new WebJarController(crypto, configuration, "assets/libs");
        assertThat(root).isNotNull();

        assertThat(controller.indexSize()).isEqualTo(0);
        assertThat(controller.libs.size()).isEqualTo(0);
    }

    @Test
    public void testOnEmptyFolder() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        Crypto crypto = mock(Crypto.class);
        root = new File("target/wisdom-test");
        webjars.mkdirs();
        when(configuration.getBaseDir()).thenReturn(root);

        WebJarController controller = new WebJarController(crypto, configuration, "assets/libs");
        assertThat(root).isNotNull();

        assertThat(controller.indexSize()).isEqualTo(0);
        assertThat(controller.libs.size()).isEqualTo(0);
    }

    @Test
    public void testOnFolderWithOneLibrary() throws IOException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        Crypto crypto = mock(Crypto.class);
        root = new File("target/wisdom-test");
        webjars.mkdirs();
        // Copy autobahn
        FileUtils.copyDirectory(new File("target/test-classes/autobahnjs/0.8.2"), new File(webjars,
                "autobahnjs/0.8.2"));

        when(configuration.getBaseDir()).thenReturn(root);

        final WebJarController controller = new WebJarController(crypto, configuration, "assets/libs");
        assertThat(root).isNotNull();
        assertThat(controller.indexSize()).isEqualTo(1);
        assertThat(controller.libs.size()).isEqualTo(1);

        assertThat(controller.libs.get(0).name).isEqualTo("autobahnjs");
        assertThat(controller.libs.get(0).version).isEqualTo("0.8.2");
        assertThat(controller.libs.get(0).contains("autobahn.min.js")).isTrue();
        assertThat(controller.libs.get(0).contains("autobahn.js")).isFalse();
        assertThat(controller.libs.get(0).get("autobahn.min.js", mock(Context.class),
                configuration, crypto).getStatusCode()).isEqualTo(200);
        assertThat(controller.libs.get(0).get("autobahn.js", mock(Context.class),
                configuration, crypto).getStatusCode()).isEqualTo(404);

        // Try to serve.
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);

        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testAlternativeUrls() throws IOException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        Crypto crypto = mock(Crypto.class);
        root = new File("target/wisdom-test");
        webjars.mkdirs();
        // Copy autobahn
        FileUtils.copyDirectory(new File("target/test-classes/autobahnjs/0.8.2"), new File(webjars,
                "autobahnjs/0.8.2"));

        when(configuration.getBaseDir()).thenReturn(root);

        final WebJarController controller = new WebJarController(crypto, configuration, "assets/libs");
        assertThat(root).isNotNull();
        assertThat(controller.indexSize()).isEqualTo(1);
        assertThat(controller.libs.size()).isEqualTo(1);

        // Just the file
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);

        // Just the library and file
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahnjs/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);

        // Just the library/version/file
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahnjs/0.8.2/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);

        // Missing version
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahnjs/0.8.x/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(404);

        // Missing library
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autoba/0.8.2/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testAlternativeUrlsWhenTwoVersionOfTheSameLibAreThere() throws IOException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        Crypto crypto = mock(Crypto.class);
        root = new File("target/wisdom-test");
        webjars.mkdirs();
        // Copy autobahn
        FileUtils.copyDirectory(new File("target/test-classes/autobahnjs/0.8.2"), new File(webjars,
                "autobahnjs/0.8.2"));
        FileUtils.copyDirectory(new File("target/test-classes/autobahnjs/0.8.2-1"), new File(webjars,
                "autobahnjs/0.8.2-1"));

        when(configuration.getBaseDir()).thenReturn(root);

        final WebJarController controller = new WebJarController(crypto, configuration, "assets/libs");
        assertThat(root).isNotNull();
        assertThat(controller.indexSize()).isEqualTo(2);
        assertThat(controller.libs.size()).isEqualTo(2);

        // Just the file
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);

        // Just the library and file
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();

            }
        }).parameter("path", "autobahnjs/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);

        // Just the library/version/file
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahnjs/0.8.2/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
        assertThat(FileUtils.readFileToString((File) result.getResult().getRenderable().content())).contains("0.8.2")
                .doesNotContain("0.8.2-1");

        // Missing version
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahnjs/0.8.x/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(404);

        // Second version
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahnjs/0.8.2-1/autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
        assertThat(FileUtils.readFileToString((File) result.getResult().getRenderable().content())).contains("0.8.2-1");
    }

    @Test
    public void testOnFolderWithTwoLibraries() throws Exception {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        Crypto crypto = mock(Crypto.class);
        root = new File("target/wisdom-test");
        webjars.mkdirs();
        // Copy autobahn
        FileUtils.copyDirectory(new File("target/test-classes/autobahnjs/0.8.2"), new File(webjars,
                "autobahnjs/0.8.2"));
        FileUtils.copyDirectory(new File("target/test-classes/highcharts"), new File(webjars, "highcharts"));

        when(configuration.getBaseDir()).thenReturn(root);

        final WebJarController controller = new WebJarController(crypto, configuration, "assets/libs");
        assertThat(root).isNotNull();
        assertThat(controller.libs.size()).isEqualTo(2);

        // Try to serve.
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);

        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "highcharts.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
        InputStream stream = result.getResult().getRenderable().render(null, null);
        assertThat(IOUtils.toString(stream))
                .contains("Highcharts JS v3.0.9 (2014-01-15)");
        IOUtils.closeQuietly(stream);

        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "adapters/mootools-adapter.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
        stream = result.getResult().getRenderable().render(null, null);
        assertThat(IOUtils.toString(stream))
                .contains("MooTools adapter");
        IOUtils.closeQuietly(stream);

        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").invoke();

        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
        stream = result.getResult().getRenderable().render(null, null);
        assertThat(IOUtils.toString(stream))
                .contains("var AUTOBAHNJS_VERSION=");
        IOUtils.closeQuietly(stream);
    }

    @Test
    public void testEtagAndCacheControl() throws IOException, InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        Crypto crypto = mock(Crypto.class);
        when(crypto.hexSHA1(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                try {
                    String value = (String) invocation.getArguments()[0];
                    MessageDigest md;
                    md = MessageDigest.getInstance(Hash.SHA1.toString());
                    md.update(value.getBytes("utf-8"));
                    byte[] digest = md.digest();
                    return String.valueOf(Hex.encodeHex(digest));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        root = new File("target/wisdom-test");
        webjars.mkdirs();
        // Copy autobahn
        FileUtils.copyDirectory(new File("target/test-classes/autobahnjs"), new File(webjars, "autobahnjs"));

        when(configuration.getBaseDir()).thenReturn(root);
        when(configuration.getWithDefault(CacheUtils.HTTP_CACHE_CONTROL_MAX_AGE,
                CacheUtils.HTTP_CACHE_CONTROL_DEFAULT)).thenReturn(CacheUtils
                .HTTP_CACHE_CONTROL_DEFAULT);
        when(configuration.getBooleanWithDefault(CacheUtils.HTTP_USE_ETAG,
                CacheUtils.HTTP_USE_ETAG_DEFAULT)).thenReturn(CacheUtils.HTTP_USE_ETAG_DEFAULT);

        final WebJarController controller = new WebJarController(crypto, configuration, "assets/libs");
        assertThat(root).isNotNull();

        // First attempt
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").invoke();


        assertThat(result.getResult().getStatusCode()).isEqualTo(200);
        assertThat(result.getResult().getHeaders().get("Cache-Control")).isEqualTo("max-age=3600");
        assertThat(result.getResult().getHeaders().get("Etag")).isNotNull();

        String lastModified = result.getResult().getHeaders().get("Last-Modified");
        String etag = result.getResult().getHeaders().get("Etag");

        // If-None-Match (ETAG check)
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").header("If-None-Match", etag).invoke();

        // Not modified.
        assertThat(result.getResult().getStatusCode()).isEqualTo(304);

        // If-Modified-Since (Date check)
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return controller.serve();
            }
        }).parameter("path", "autobahn.min.js").header("If-Modified-Since", lastModified).invoke();

        // Not modified.
        assertThat(result.getResult().getStatusCode()).isEqualTo(304);
    }
}
